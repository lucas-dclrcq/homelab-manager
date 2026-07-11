package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.MatchableCandidate
import org.hoohoot.homelab.manager.cleanup.domain.TitleMatch
import org.hoohoot.homelab.manager.cleanup.domain.TitleMatcher
import org.hoohoot.homelab.manager.cleanup.domain.VetoByTitleResult
import org.hoohoot.homelab.manager.cleanup.domain.VetoChannel
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import java.time.LocalDateTime
import java.util.UUID

// Veto par titre depuis le bot Matrix : `!johnny garde <titre>`
@ApplicationScoped
class VetoByTitle(
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val protections: Protections,
) {
    private val titleMatcher = TitleMatcher()

    suspend operator fun invoke(rawTitle: String, username: String): VetoByTitleResult {
        val campaign = campaigns.findActive() ?: return VetoByTitleResult.NoCampaign
        val proposed = candidates.listByCampaign(requireNotNull(campaign.id))
            .filter { it.status == CleanupCandidateEntity.STATUS_PROPOSED }

        val matchables = proposed.map {
            MatchableCandidate(
                candidateId = requireNotNull(it.id),
                title = it.title,
                mediaKind = it.mediaKind,
                radarrMovieId = it.radarrMovieId,
                sonarrSeriesId = it.sonarrSeriesId,
                seasonNumber = it.seasonNumber,
            )
        }

        return when (val match = titleMatcher.match(rawTitle, matchables)) {
            TitleMatch.NoMatch -> VetoByTitleResult.NoMatch(proposed.map { it.displayTitle() }.distinct())
            is TitleMatch.Ambiguous -> VetoByTitleResult.Ambiguous(match.titles)
            is TitleMatch.Matched -> protect(match.candidates, proposed, username)
        }
    }

    private suspend fun protect(
        matched: List<MatchableCandidate>,
        proposed: List<CleanupCandidateEntity>,
        username: String,
    ): VetoByTitleResult {
        val entities = proposed.filter { entity -> matched.any { it.candidateId == entity.id } }
        entities.forEach { entity ->
            candidates.update(requireNotNull(entity.id)) {
                it.status = CleanupCandidateEntity.STATUS_PROTECTED
                it.protectedBy = username
                it.protectedVia = VetoChannel.BOT.name
                it.protectedAt = LocalDateTime.now()
            }
        }

        // Un veto bot sur une série protège la série entière, pas seulement les saisons candidates
        val sample = entities.first()
        saveProtectionIfMissing(sample, username)

        return VetoByTitleResult.Ok(entities.map { it.displayTitle() })
    }

    private suspend fun saveProtectionIfMissing(candidate: CleanupCandidateEntity, username: String) {
        val isMovie = candidate.mediaKind == CleanupCandidateEntity.KIND_MOVIE
        val alreadyCovered = protections.all().any { protection ->
            if (isMovie) {
                protection.covers(candidate.radarrMovieId, null, null)
            } else {
                protection.mediaKind == CleanupProtectionEntity.KIND_SERIES &&
                    protection.sonarrSeriesId == candidate.sonarrSeriesId
            }
        }
        if (alreadyCovered) return

        protections.save(
            CleanupProtectionEntity().apply {
                id = UUID.randomUUID()
                mediaKind = if (isMovie) CleanupProtectionEntity.KIND_MOVIE else CleanupProtectionEntity.KIND_SERIES
                radarrMovieId = candidate.radarrMovieId
                sonarrSeriesId = candidate.sonarrSeriesId
                seasonNumber = null
                title = candidate.title
                year = candidate.year
                posterUrl = candidate.posterUrl
                protectedBy = username
                source = CleanupProtectionEntity.SOURCE_VETO
                createdAt = LocalDateTime.now()
            },
        )
    }
}
