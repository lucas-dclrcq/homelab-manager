package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.cleanup.domain.CleanupMovie
import org.hoohoot.homelab.manager.cleanup.domain.DeleteOutcome
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieEraser
import org.hoohoot.homelab.manager.notifications.api.requester
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient

@ApplicationScoped
class RadarrCleanupAdapter(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
) : MovieCatalog, MovieEraser {

    override suspend fun allMovies(): List<CleanupMovie> {
        val tagLabels = tagLabels()
        return radarrRestClient.getMovies().orEmpty().mapNotNull { it.toCleanupMovie(tagLabels) }
    }

    override suspend fun deleteMovie(radarrMovieId: Int, sizeBytes: Long): DeleteOutcome = try {
        radarrRestClient.deleteMovie(radarrMovieId, deleteFiles = true, addImportExclusion = false)
        DeleteOutcome.Deleted(sizeBytes)
    } catch (exception: WebApplicationException) {
        if (exception.response?.status == 404) DeleteOutcome.AlreadyGone
        else DeleteOutcome.Failed("Radarr a répondu ${exception.response?.status} : ${exception.message}")
    }

    // Best-effort : sans les tags on perd juste l'attribution du demandeur
    private suspend fun tagLabels(): Map<Int, String> = try {
        radarrRestClient.getTags().orEmpty()
            .mapNotNull { tag -> tag.id?.let { id -> tag.label?.let { id to it } } }
            .toMap()
    } catch (exception: Exception) {
        Log.warn("Cleanup: could not fetch Radarr tags, requesters will be unknown", exception)
        emptyMap()
    }

    private fun RadarrMovie.toCleanupMovie(tagLabels: Map<Int, String>): CleanupMovie? {
        val movieId = id ?: return null
        val movieTitle = title ?: return null
        return CleanupMovie(
            radarrMovieId = movieId,
            title = movieTitle,
            year = year,
            posterUrl = images.firstOrNull { it.coverType == "poster" }?.remoteUrl,
            tmdbId = tmdbId,
            imdbId = imdbId,
            sizeBytes = sizeOnDisk ?: movieFile?.size ?: 0,
            downloadedAt = (movieFile?.dateAdded ?: added).toUtcLocalDateTime(),
            requester = tags.mapNotNull { tagLabels[it] }.requester(),
            hasFile = hasFile == true,
        )
    }
}
