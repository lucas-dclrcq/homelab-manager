package org.hoohoot.homelab.manager.problems.infra

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.problems.domain.BlockedImport
import org.hoohoot.homelab.manager.problems.domain.LibraryMovie
import org.hoohoot.homelab.manager.problems.domain.Release
import org.hoohoot.homelab.manager.problems.domain.isForceableRejection
import org.hoohoot.homelab.manager.problems.domain.ports.ImportQueue
import org.hoohoot.homelab.manager.problems.domain.ports.MovieLibrary
import org.hoohoot.homelab.manager.problems.domain.ports.Releases
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrGrabRequest
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrManualImportCommand
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrManualImportFile
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrQualityProfile
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrQualityProfileItem
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRelease
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient

@ApplicationScoped
class RadarrProblemsAdapter(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
) : MovieLibrary, Releases, ImportQueue {

    override suspend fun allMovies(): List<LibraryMovie> {
        val resolutionByProfile = desiredResolutionByProfile()
        return radarrRestClient.getMovies().orEmpty().mapNotNull { it.toLibraryMovie(resolutionByProfile) }
    }

    override suspend fun searchForMovie(radarrMovieId: Int): List<Release> =
        radarrRestClient.searchReleases(radarrMovieId).orEmpty().mapNotNull { it.toRelease() }

    override suspend fun grab(guid: String, indexerId: Int) {
        radarrRestClient.grabRelease(RadarrGrabRequest(guid, indexerId))
    }

    // importBlocked depuis Radarr 5.3, importPending avant : on accepte les deux,
    // le vrai gating se fait sur le message de rejet
    override suspend fun blockedImports(): List<BlockedImport> =
        radarrRestClient.getQueue(page = 1, pageSize = 1000, includeUnknownMovieItems = false)
            ?.records.orEmpty()
            .filter { it.trackedDownloadState == "importBlocked" || it.trackedDownloadState == "importPending" }
            .mapNotNull { record ->
                BlockedImport(
                    radarrMovieId = record.movieId ?: return@mapNotNull null,
                    downloadId = record.downloadId ?: return@mapNotNull null,
                    title = record.title,
                    statusMessages = record.statusMessages.flatMap { it.messages },
                )
            }

    override suspend fun forceImport(downloadId: String, radarrMovieId: Int): Boolean {
        val files = radarrRestClient.getManualImport(downloadId, filterExistingFiles = true).orEmpty()
            .filter { it.movie?.id == radarrMovieId }
            // Un fichier rejeté pour un autre motif (sample, film inconnu...) n'est jamais forcé
            .filter { item -> item.rejections.all { rejection -> rejection.reason?.let(::isForceableRejection) == true } }
            .mapNotNull { item ->
                val path = item.path ?: return@mapNotNull null
                RadarrManualImportFile(
                    path = path,
                    movieId = radarrMovieId,
                    quality = item.quality,
                    languages = item.languages,
                    downloadId = downloadId,
                )
            }
        if (files.isEmpty()) return false
        radarrRestClient.postCommand(RadarrManualImportCommand(files = files))
        return true
    }

    // Résolution cible (cutoff) de chaque profil de qualité. Best-effort : en cas d'échec, on
    // recommande de façon permissive plutôt que de casser la recherche de films.
    private suspend fun desiredResolutionByProfile(): Map<Int, String> =
        try {
            radarrRestClient.getQualityProfiles().orEmpty()
                .mapNotNull { profile ->
                    val id = profile.id ?: return@mapNotNull null
                    val resolution = profile.cutoffResolution() ?: return@mapNotNull null
                    id to resolution.toString()
                }
                .toMap()
        } catch (exception: Exception) {
            Log.warn("Problems: could not fetch Radarr quality profiles, releases will not be filtered by resolution", exception)
            emptyMap()
        }

    // Le cutoff référence soit l'id d'une qualité (feuille), soit l'id d'un groupe de qualités
    private fun RadarrQualityProfile.cutoffResolution(): Int? {
        val target = cutoff ?: return null
        fun find(items: List<RadarrQualityProfileItem>): Int? {
            for (item in items) {
                if (item.quality?.id == target) return item.quality.resolution
                if (item.id == target && item.items.isNotEmpty()) {
                    return item.items.firstNotNullOfOrNull { it.quality?.resolution }
                }
                find(item.items)?.let { return it }
            }
            return null
        }
        return find(items)
    }

    private fun RadarrMovie.toLibraryMovie(resolutionByProfile: Map<Int, String>): LibraryMovie? {
        val movieId = id ?: return null
        val movieTitle = title ?: return null
        return LibraryMovie(
            radarrMovieId = movieId,
            title = movieTitle,
            year = year,
            posterUrl = images.firstOrNull { it.coverType == "poster" }?.remoteUrl,
            overview = overview,
            currentQuality = movieFile?.quality?.quality?.name,
            currentLanguages = movieFile?.languages.orEmpty().mapNotNull { it.name },
            desiredResolution = qualityProfileId?.let { resolutionByProfile[it] },
        )
    }

    private fun RadarrRelease.toRelease(): Release? {
        val releaseGuid = guid ?: return null
        val releaseIndexerId = indexerId ?: return null
        val releaseTitle = title ?: return null
        return Release(
            guid = releaseGuid,
            indexerId = releaseIndexerId,
            indexer = indexer,
            title = releaseTitle,
            quality = quality?.quality?.name,
            size = size,
            age = age,
            seeders = seeders,
            leechers = leechers,
            protocol = protocol,
            rejected = rejected == true,
            rejections = rejections,
            languages = languages.mapNotNull { it.name },
        )
    }
}
