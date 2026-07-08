package org.hoohoot.homelab.manager.corrector.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.corrector.domain.LibraryMovie
import org.hoohoot.homelab.manager.corrector.domain.Release
import org.hoohoot.homelab.manager.corrector.domain.ports.MovieLibrary
import org.hoohoot.homelab.manager.corrector.domain.ports.Releases
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrGrabRequest
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRelease
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient

@ApplicationScoped
class RadarrCorrectorAdapter(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
) : MovieLibrary, Releases {

    override suspend fun allMovies(): List<LibraryMovie> =
        radarrRestClient.getMovies().orEmpty().mapNotNull { it.toLibraryMovie() }

    override suspend fun searchForMovie(radarrMovieId: Int): List<Release> =
        radarrRestClient.searchReleases(radarrMovieId).orEmpty().mapNotNull { it.toRelease() }

    override suspend fun grab(guid: String, indexerId: Int) {
        radarrRestClient.grabRelease(RadarrGrabRequest(guid, indexerId))
    }

    private fun RadarrMovie.toLibraryMovie(): LibraryMovie? {
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
