package org.hoohoot.homelab.manager.cleanup

import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.cleanup.domain.CleanupMovie
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeries
import org.hoohoot.homelab.manager.cleanup.domain.Correlation
import org.hoohoot.homelab.manager.cleanup.domain.JellyfinEntryType
import org.hoohoot.homelab.manager.cleanup.domain.JellyfinLibraryEntry
import org.hoohoot.homelab.manager.cleanup.domain.MediaCorrelator
import org.hoohoot.homelab.manager.cleanup.domain.MovieWatchAggregate
import org.hoohoot.homelab.manager.cleanup.domain.SeasonWatchAggregate
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MediaCorrelatorTest {

    private val lastWatch: LocalDateTime = LocalDateTime.parse("2026-06-15T21:00:00")

    private fun movie(
        title: String = "Un Film",
        year: Int? = null,
        tmdbId: Int? = null,
        imdbId: String? = null,
    ) = CleanupMovie(
        radarrMovieId = 1,
        title = title,
        year = year,
        posterUrl = null,
        tmdbId = tmdbId,
        imdbId = imdbId,
        sizeBytes = 10_000_000_000,
        downloadedAt = null,
        requester = null,
        hasFile = true,
    )

    private fun series(
        title: String = "Une Serie",
        year: Int? = null,
        tvdbId: Int? = null,
        imdbId: String? = null,
    ) = CleanupSeries(
        sonarrSeriesId = 1,
        title = title,
        year = year,
        posterUrl = null,
        tvdbId = tvdbId,
        imdbId = imdbId,
        continuing = false,
        addedAt = null,
        requester = null,
        seasons = emptyList(),
    )

    private fun movieEntry(
        itemId: String,
        name: String,
        year: Int? = null,
        tmdbId: String? = null,
        imdbId: String? = null,
    ) = JellyfinLibraryEntry(
        itemId = itemId,
        name = name,
        productionYear = year,
        type = JellyfinEntryType.MOVIE,
        tmdbId = tmdbId,
        imdbId = imdbId,
        tvdbId = null,
    )

    private fun seriesEntry(
        itemId: String,
        name: String,
        tvdbId: String? = null,
    ) = JellyfinLibraryEntry(
        itemId = itemId,
        name = name,
        productionYear = null,
        type = JellyfinEntryType.SERIES,
        tmdbId = null,
        imdbId = null,
        tvdbId = tvdbId,
    )

    private fun movieWatch(
        itemId: String,
        itemName: String = "peu importe",
        lastWatchedAt: LocalDateTime = lastWatch,
        completedBySomeone: Boolean = false,
    ) = MovieWatchAggregate(
        itemId = itemId,
        itemName = itemName,
        lastWatchedAt = lastWatchedAt,
        completedBySomeone = completedBySomeone,
        lastInProgressAt = null,
    )

    private fun seasonWatch(
        seriesId: String?,
        seasonNumber: Int?,
        seriesName: String? = null,
        lastWatchedAt: LocalDateTime = lastWatch,
    ) = SeasonWatchAggregate(
        seriesId = seriesId,
        seriesName = seriesName,
        seasonNumber = seasonNumber,
        lastWatchedAt = lastWatchedAt,
        completedBySomeone = false,
        lastInProgressAt = null,
    )

    private fun correlator(
        entries: List<JellyfinLibraryEntry> = emptyList(),
        movieWatches: List<MovieWatchAggregate> = emptyList(),
        seasonWatches: List<SeasonWatchAggregate> = emptyList(),
    ) = MediaCorrelator(entries, movieWatches, seasonWatches)

    @Test
    fun `un film est correle par tmdbId meme si les titres different`() {
        val correlator = correlator(
            entries = listOf(movieEntry(itemId = "jf-1", name = "Titre Jellyfin", tmdbId = "603")),
            movieWatches = listOf(movieWatch(itemId = "jf-1", completedBySomeone = true)),
        )

        val watch = correlator.watchOf(movie(title = "Titre Radarr", tmdbId = 603))

        assertThat(watch.correlation).isEqualTo(Correlation.PROVIDER_ID)
        assertThat(watch.lastWatchedAt).isEqualTo(lastWatch)
        assertThat(watch.completedBySomeone).isTrue()
        assertThat(watch.startedBySomeone).isTrue()
    }

    @Test
    fun `un film sans tmdbId est correle par imdbId`() {
        val correlator = correlator(
            entries = listOf(movieEntry(itemId = "jf-1", name = "Un Film", imdbId = "tt0111161")),
            movieWatches = listOf(movieWatch(itemId = "jf-1")),
        )

        val watch = correlator.watchOf(movie(imdbId = "tt0111161"))

        assertThat(watch.correlation).isEqualTo(Correlation.PROVIDER_ID)
        assertThat(watch.startedBySomeone).isTrue()
    }

    @Test
    fun `sans provider id le titre normalise fait le lien malgre accents et ponctuation`() {
        val correlator = correlator(
            entries = listOf(movieEntry(itemId = "jf-2", name = "amelie")),
            movieWatches = listOf(movieWatch(itemId = "jf-2")),
        )

        val watch = correlator.watchOf(movie(title = "Amélie !"))

        assertThat(watch.correlation).isEqualTo(Correlation.TITLE)
        assertThat(watch.lastWatchedAt).isEqualTo(lastWatch)
    }

    @Test
    fun `deux homonymes sans annee pour departager rendent le film non correle`() {
        val correlator = correlator(
            entries = listOf(
                movieEntry(itemId = "jf-a", name = "Hamlet", year = 1990),
                movieEntry(itemId = "jf-b", name = "Hamlet", year = 1996),
            ),
            movieWatches = listOf(
                movieWatch(itemId = "jf-a", itemName = "Hamlet"),
                movieWatch(itemId = "jf-b", itemName = "Hamlet"),
            ),
        )

        val watch = correlator.watchOf(movie(title = "Hamlet", year = null))

        assertThat(watch.correlation).isEqualTo(Correlation.NONE)
        assertThat(watch.startedBySomeone).isFalse()
        assertThat(watch.lastWatchedAt).isNull()
    }

    @Test
    fun `deux homonymes sont departages par l'annee a un an pres`() {
        val recentWatch = lastWatch.plusDays(3)
        val correlator = correlator(
            entries = listOf(
                movieEntry(itemId = "jf-old", name = "Dune", year = 1984),
                movieEntry(itemId = "jf-new", name = "Dune", year = 2021),
            ),
            movieWatches = listOf(movieWatch(itemId = "jf-new", lastWatchedAt = recentWatch)),
        )

        val watch = correlator.watchOf(movie(title = "Dune", year = 2022))

        assertThat(watch.correlation).isEqualTo(Correlation.TITLE)
        assertThat(watch.lastWatchedAt).isEqualTo(recentWatch)
    }

    @Test
    fun `un film absent de Jellyfin est retrouve dans l'historique par nom d'item unique`() {
        val correlator = correlator(
            movieWatches = listOf(movieWatch(itemId = "jf-h", itemName = "Vieux Film")),
        )

        val watch = correlator.watchOf(movie(title = "Vieux Film"))

        assertThat(watch.correlation).isEqualTo(Correlation.TITLE)
        assertThat(watch.startedBySomeone).isTrue()
        assertThat(watch.lastWatchedAt).isEqualTo(lastWatch)
    }

    @Test
    fun `une saison est correlee par tvdbId sur le couple seriesId numero de saison`() {
        val correlator = correlator(
            entries = listOf(seriesEntry(itemId = "jf-s", name = "Une Serie", tvdbId = "789")),
            seasonWatches = listOf(seasonWatch(seriesId = "jf-s", seasonNumber = 2)),
        )

        val watch = correlator.watchOfSeason(series(tvdbId = 789), 2)

        assertThat(watch.correlation).isEqualTo(Correlation.PROVIDER_ID)
        assertThat(watch.lastWatchedAt).isEqualTo(lastWatch)
        assertThat(watch.startedBySomeone).isTrue()
    }

    @Test
    fun `une saison jamais vue d'une serie vue reste correlee et garde la trace du dernier visionnage serie`() {
        val correlator = correlator(
            entries = listOf(seriesEntry(itemId = "jf-s", name = "Une Serie", tvdbId = "789")),
            seasonWatches = listOf(seasonWatch(seriesId = "jf-s", seasonNumber = 2)),
        )

        val watch = correlator.watchOfSeason(series(tvdbId = 789), 5)

        assertThat(watch.correlation).isEqualTo(Correlation.PROVIDER_ID)
        assertThat(watch.startedBySomeone).isFalse()
        assertThat(watch.lastWatchedAt).isNull()
        assertThat(correlator.seriesLastWatchedAt(series(tvdbId = 789))).isEqualTo(lastWatch)
    }
}
