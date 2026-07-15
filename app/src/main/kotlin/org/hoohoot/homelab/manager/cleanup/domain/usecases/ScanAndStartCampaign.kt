package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.CampaignTrigger
import org.hoohoot.homelab.manager.cleanup.domain.CandidateScorer
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.domain.CleanupMovie
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeries
import org.hoohoot.homelab.manager.cleanup.domain.Evaluation
import org.hoohoot.homelab.manager.cleanup.domain.MediaCorrelator
import org.hoohoot.homelab.manager.cleanup.domain.RequesterProfile
import org.hoohoot.homelab.manager.cleanup.domain.ScanResult
import org.hoohoot.homelab.manager.cleanup.domain.ScoreBreakdown
import org.hoohoot.homelab.manager.cleanup.domain.ScoringInput
import org.hoohoot.homelab.manager.cleanup.domain.ports.ActiveProblems
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupConfigStore
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.cleanup.domain.ports.DiskSpaceGauge
import org.hoohoot.homelab.manager.cleanup.domain.ports.JellyfinCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.MemberStatuses
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.PlaybackHistory
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeriesCatalog
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ScanAndStartCampaign(
    private val configStore: CleanupConfigStore,
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val protections: Protections,
    private val activeProblems: ActiveProblems,
    private val movieCatalog: MovieCatalog,
    private val seriesCatalog: SeriesCatalog,
    private val jellyfinCatalog: JellyfinCatalog,
    private val playbackHistory: PlaybackHistory,
    private val memberStatuses: MemberStatuses,
    private val diskSpaceGauge: DiskSpaceGauge,
    private val notifier: CleanupNotifier,
) {
    suspend operator fun invoke(trigger: CampaignTrigger, overrideTargetBytes: Long? = null): ScanResult {
        val config = configStore.effective()

        if (campaigns.findActive() != null) return ScanResult.AlreadyActive

        val freeBytes = diskSpaceGauge.snapshotFree(config.diskPath) ?: return ScanResult.DiskSpaceUnknown
        if (trigger == CampaignTrigger.AUTO && freeBytes >= config.thresholdFreeBytes) {
            return ScanResult.ThresholdNotReached(freeBytes)
        }

        val targetToFree = overrideTargetBytes ?: (config.targetFreeBytes - freeBytes)
        if (targetToFree <= 0) return ScanResult.ThresholdNotReached(freeBytes)

        val now = LocalDateTime.now()
        val scorer = CandidateScorer(config, now)
        val correlator = MediaCorrelator(
            jellyfinEntries = jellyfinCatalog.libraryEntries(),
            movieWatches = playbackHistory.movieWatchAggregates(),
            seasonWatches = playbackHistory.seasonWatchAggregates(),
        )
        val userActivity = playbackHistory.userLastActivity()
        val members = memberStatuses.memberStatuses()
        val allProtections = protections.all()
        val problemIds = activeProblems.activeMediaIds()

        fun requesterProfile(requester: String?): RequesterProfile? = requester?.let {
            RequesterProfile(
                username = it,
                activeMember = members[it.lowercase()],
                lastActivityAt = userActivity[it.lowercase()],
            )
        }

        val movieProposals = movieCatalog.allMovies()
            .asSequence()
            .filter { it.hasFile && it.sizeBytes > 0 }
            .filterNot { it.radarrMovieId in problemIds.radarrMovieIds }
            .filterNot { movie -> allProtections.any { it.covers(movie.radarrMovieId, null, null) } }
            .mapNotNull { movie ->
                val watch = correlator.watchOf(movie)
                val input = ScoringInput(movie.sizeBytes, movie.downloadedAt, watch, requesterProfile(movie.requester))
                (scorer.evaluate(input) as? Evaluation.Scored)?.let { movie.toProposal(it.breakdown) }
            }
            .toList()

        val seasonProposals = seriesProposals(
            config = config,
            scorer = scorer,
            correlator = correlator,
            allProtections = allProtections,
            problemIds = problemIds.sonarrSeriesIds,
            now = now,
            requesterProfile = ::requesterProfile,
        )

        val selected = selectProposals(movieProposals + seasonProposals, targetToFree, config.maxCandidates, config.minScore)
        if (selected.isEmpty()) return ScanResult.NoCandidates

        val campaign = campaigns.save(
            CleanupCampaignEntity().apply {
                id = UUID.randomUUID()
                status = CleanupCampaignEntity.STATUS_ANNOUNCED
                triggerType = trigger.name
                diskPath = config.diskPath
                freeBytesAtStart = freeBytes
                thresholdBytes = config.thresholdFreeBytes
                targetBytesToFree = targetToFree
                graceEndsAt = now.plusDays(config.graceDays)
                createdAt = now
                updatedAt = now
            },
        )

        val entities = selected.map { it.toEntity(campaign.id!!, now) }
        candidates.saveAll(entities)

        try {
            val eventId = notifier.announceCampaign(campaign, entities)
            if (eventId != null) campaigns.update(campaign.id!!) { it.announcementEventId = eventId }
        } catch (exception: Exception) {
            Log.error("Cleanup: campaign announcement failed", exception)
        }

        return ScanResult.Started(campaign, entities.size)
    }

    private suspend fun seriesProposals(
        config: CleanupConfig,
        scorer: CandidateScorer,
        correlator: MediaCorrelator,
        allProtections: List<CleanupProtectionEntity>,
        problemIds: Set<Int>,
        now: LocalDateTime,
        requesterProfile: (String?) -> RequesterProfile?,
    ): List<Proposal> {
        val proposals = mutableListOf<Proposal>()
        for (series in seriesCatalog.allSeries()) {
            if (series.sonarrSeriesId in problemIds) continue
            if (allProtections.any { it.covers(null, series.sonarrSeriesId, null) && it.mediaKind == CleanupProtectionEntity.KIND_SERIES }) continue

            val seriesLastWatched = correlator.seriesLastWatchedAt(series)
            if (seriesLastWatched != null &&
                Duration.between(seriesLastWatched, now).toDays() < config.recentSeriesWatchDays
            ) continue

            val lastSeasonNumber = series.seasons.maxOfOrNull { it.seasonNumber }
            val eligibleSeasons = series.seasons.filter { season ->
                season.episodeFileCount > 0 &&
                    season.sizeBytes > 0 &&
                    !(series.continuing && season.seasonNumber == lastSeasonNumber) &&
                    allProtections.none { it.covers(null, series.sonarrSeriesId, season.seasonNumber) }
            }
            if (eligibleSeasons.isEmpty()) continue

            val downloadDates = seriesCatalog.seasonDownloadDates(series.sonarrSeriesId)

            for (season in eligibleSeasons) {
                val watch = correlator.watchOfSeason(series, season.seasonNumber)
                val downloadedAt = downloadDates[season.seasonNumber] ?: season.previousAiring ?: series.addedAt
                val input = ScoringInput(season.sizeBytes, downloadedAt, watch, requesterProfile(series.requester))
                (scorer.evaluate(input) as? Evaluation.Scored)
                    ?.let { proposals += series.toProposal(season.seasonNumber, season.sizeBytes, it.breakdown) }
            }
        }
        return proposals
    }

    private fun selectProposals(
        proposals: List<Proposal>,
        targetToFree: Long,
        maxCandidates: Int,
        minScore: Double,
    ): List<Proposal> {
        val selected = mutableListOf<Proposal>()
        var cumulated = 0L
        for (proposal in proposals.filter { it.breakdown.total >= minScore }.sortedByDescending { it.breakdown.total }) {
            if (cumulated >= targetToFree || selected.size >= maxCandidates) break
            selected += proposal
            cumulated += proposal.sizeBytes
        }
        return selected
    }

    private data class Proposal(
        val mediaKind: String,
        val radarrMovieId: Int?,
        val sonarrSeriesId: Int?,
        val seasonNumber: Int?,
        val title: String,
        val year: Int?,
        val posterUrl: String?,
        val sizeBytes: Long,
        val requester: String?,
        val breakdown: ScoreBreakdown,
    )

    private fun CleanupMovie.toProposal(breakdown: ScoreBreakdown) = Proposal(
        mediaKind = CleanupCandidateEntity.KIND_MOVIE,
        radarrMovieId = radarrMovieId,
        sonarrSeriesId = null,
        seasonNumber = null,
        title = title,
        year = year,
        posterUrl = posterUrl,
        sizeBytes = sizeBytes,
        requester = requester,
        breakdown = breakdown,
    )

    private fun CleanupSeries.toProposal(seasonNumber: Int, sizeBytes: Long, breakdown: ScoreBreakdown) = Proposal(
        mediaKind = CleanupCandidateEntity.KIND_SEASON,
        radarrMovieId = null,
        sonarrSeriesId = sonarrSeriesId,
        seasonNumber = seasonNumber,
        title = title,
        year = year,
        posterUrl = posterUrl,
        sizeBytes = sizeBytes,
        requester = requester,
        breakdown = breakdown,
    )

    private fun Proposal.toEntity(campaignId: UUID, now: LocalDateTime) = CleanupCandidateEntity().apply {
        id = UUID.randomUUID()
        this.campaignId = campaignId
        mediaKind = this@toEntity.mediaKind
        radarrMovieId = this@toEntity.radarrMovieId
        sonarrSeriesId = this@toEntity.sonarrSeriesId
        seasonNumber = this@toEntity.seasonNumber
        title = this@toEntity.title
        year = this@toEntity.year
        posterUrl = this@toEntity.posterUrl
        sizeBytes = this@toEntity.sizeBytes
        requester = this@toEntity.requester
        score = BigDecimal.valueOf(breakdown.total)
        scoreBreakdown = breakdown
        status = CleanupCandidateEntity.STATUS_PROPOSED
        createdAt = now
        updatedAt = now
    }
}
