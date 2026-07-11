package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeason
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeries
import org.hoohoot.homelab.manager.cleanup.domain.DeleteOutcome
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeasonEraser
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeriesCatalog
import org.hoohoot.homelab.manager.notifications.api.requester
import org.hoohoot.homelab.manager.shared.arr.sonarr.Series
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrRestClient
import java.time.LocalDateTime

@ApplicationScoped
class SonarrCleanupAdapter(
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
) : SeriesCatalog, SeasonEraser {

    override suspend fun allSeries(): List<CleanupSeries> {
        val tagLabels = tagLabels()
        return sonarrRestClient.getSeries().orEmpty().mapNotNull { it.toCleanupSeries(tagLabels) }
    }

    override suspend fun seasonDownloadDates(sonarrSeriesId: Int): Map<Int, LocalDateTime> =
        sonarrRestClient.getEpisodeFiles(sonarrSeriesId).orEmpty()
            .mapNotNull { file ->
                val season = file.seasonNumber ?: return@mapNotNull null
                val addedAt = file.dateAdded.toUtcLocalDateTime() ?: return@mapNotNull null
                season to addedAt
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, dates) -> dates.max() }

    override suspend fun deleteSeason(sonarrSeriesId: Int, seasonNumber: Int): DeleteOutcome {
        val series = try {
            sonarrRestClient.getSeriesById(sonarrSeriesId) ?: return DeleteOutcome.AlreadyGone
        } catch (exception: WebApplicationException) {
            return if (exception.response?.status == 404) DeleteOutcome.AlreadyGone
            else DeleteOutcome.Failed("Sonarr a répondu ${exception.response?.status} : ${exception.message}")
        }

        // Unmonitor d'abord : sinon Sonarr relance la recherche des épisodes supprimés
        val unmonitored = series.copy(
            seasons = series.seasons?.map { season ->
                if (season.seasonNumber == seasonNumber) season.copy(monitored = false) else season
            },
        )
        sonarrRestClient.updateSeries(sonarrSeriesId, unmonitored)

        val seasonFiles = sonarrRestClient.getEpisodeFiles(sonarrSeriesId).orEmpty()
            .filter { it.seasonNumber == seasonNumber }
        if (seasonFiles.isEmpty()) return DeleteOutcome.AlreadyGone

        var freed = 0L
        for (file in seasonFiles) {
            val fileId = file.id ?: continue
            try {
                sonarrRestClient.deleteEpisodeFile(fileId)
                freed += file.size ?: 0
            } catch (exception: WebApplicationException) {
                if (exception.response?.status == 404) continue
                return DeleteOutcome.Failed(
                    "suppression interrompue (fichier $fileId : ${exception.response?.status}), " +
                        "${formatBytes(freed)} déjà libérés — rejouable",
                )
            }
        }
        return DeleteOutcome.Deleted(freed)
    }

    private suspend fun tagLabels(): Map<Int, String> = try {
        sonarrRestClient.getTags().orEmpty()
            .mapNotNull { tag -> tag.id?.let { id -> tag.label?.let { id to it } } }
            .toMap()
    } catch (exception: Exception) {
        Log.warn("Cleanup: could not fetch Sonarr tags, requesters will be unknown", exception)
        emptyMap()
    }

    private fun Series.toCleanupSeries(tagLabels: Map<Int, String>): CleanupSeries? {
        val seriesId = id ?: return null
        val seriesTitle = title ?: return null
        return CleanupSeries(
            sonarrSeriesId = seriesId,
            title = seriesTitle,
            year = year,
            posterUrl = images?.firstOrNull { it.coverType == "poster" }?.remoteUrl,
            tvdbId = tvdbId,
            imdbId = imdbId,
            continuing = ended?.not() ?: (status == "continuing"),
            addedAt = added.toUtcLocalDateTime(),
            requester = tags?.mapNotNull { tagLabels[it] }.requester(),
            seasons = seasons.orEmpty().mapNotNull { season ->
                val number = season.seasonNumber ?: return@mapNotNull null
                CleanupSeason(
                    seasonNumber = number,
                    episodeFileCount = season.statistics?.episodeFileCount ?: 0,
                    sizeBytes = season.statistics?.sizeOnDisk ?: 0,
                    previousAiring = season.statistics?.previousAiring.toUtcLocalDateTime(),
                )
            },
        )
    }

    private fun formatBytes(bytes: Long): String = "%.1f Go".format(bytes / 1e9)
}
