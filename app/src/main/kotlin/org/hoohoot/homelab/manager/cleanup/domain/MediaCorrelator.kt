package org.hoohoot.homelab.manager.cleanup.domain

import java.time.LocalDateTime

/**
 * Corrèle les médias Radarr/Sonarr avec la bibliothèque et l'historique de visionnage Jellyfin.
 * Priorité aux provider ids (TMDB/IMDB/TVDB), fallback sur le titre normalisé ; en cas
 * d'ambiguïté le média est traité comme non corrélé (le scoring plafonne alors sa composante
 * « jamais visionné » par prudence).
 */
class MediaCorrelator(
    jellyfinEntries: List<JellyfinLibraryEntry>,
    movieWatches: List<MovieWatchAggregate>,
    seasonWatches: List<SeasonWatchAggregate>,
) {
    private val movieEntries = jellyfinEntries.filter { it.type == JellyfinEntryType.MOVIE }
    private val seriesEntries = jellyfinEntries.filter { it.type == JellyfinEntryType.SERIES }

    private val movieEntriesByTmdb = movieEntries.mapNotNull { entry -> entry.tmdbId?.let { it to entry } }.toMap()
    private val movieEntriesByImdb = movieEntries.mapNotNull { entry -> entry.imdbId?.let { it to entry } }.toMap()
    private val movieEntriesByTitle = movieEntries.groupBy { Titles.normalize(it.name) }

    private val seriesEntriesByTvdb = seriesEntries.mapNotNull { entry -> entry.tvdbId?.let { it to entry } }.toMap()
    private val seriesEntriesByImdb = seriesEntries.mapNotNull { entry -> entry.imdbId?.let { it to entry } }.toMap()
    private val seriesEntriesByTitle = seriesEntries.groupBy { Titles.normalize(it.name) }

    private val movieWatchesByItemId = movieWatches.associateBy { it.itemId }
    private val movieWatchesByTitle = movieWatches.groupBy { Titles.normalize(it.itemName) }

    private val seasonWatchesBySeriesId = seasonWatches
        .filter { it.seriesId != null }
        .groupBy { it.seriesId!! }
    private val seasonWatchesBySeriesName = seasonWatches
        .filter { it.seriesName != null }
        .groupBy { Titles.normalize(it.seriesName!!) }

    fun watchOf(movie: CleanupMovie): CorrelatedWatch {
        val providerEntry = movie.tmdbId?.toString()?.let { movieEntriesByTmdb[it] }
            ?: movie.imdbId?.let { movieEntriesByImdb[it] }
        if (providerEntry != null) {
            return movieWatchesByItemId[providerEntry.itemId].toCorrelated(Correlation.PROVIDER_ID)
        }

        val titleEntry = uniqueEntryByTitle(movieEntriesByTitle, movie.title, movie.year)
        if (titleEntry != null) {
            return movieWatchesByItemId[titleEntry.itemId].toCorrelated(Correlation.TITLE)
        }

        // Le film n'est plus (ou pas) dans Jellyfin : on tente encore l'historique par nom d'item
        val watchesByName = movieWatchesByTitle[Titles.normalize(movie.title)].orEmpty()
        if (watchesByName.map { it.itemId }.distinct().size == 1) {
            return watchesByName.first().toCorrelated(Correlation.TITLE)
        }

        return uncorrelated()
    }

    fun watchOfSeason(series: CleanupSeries, seasonNumber: Int): CorrelatedWatch {
        val (correlation, watches) = seasonWatchesOf(series)
        if (correlation == Correlation.NONE) return uncorrelated()

        val seasonWatch = watches.firstOrNull { it.seasonNumber == seasonNumber }
        return CorrelatedWatch(
            correlation = correlation,
            lastWatchedAt = seasonWatch?.lastWatchedAt,
            completedBySomeone = seasonWatch?.completedBySomeone == true,
            startedBySomeone = seasonWatch != null,
            lastInProgressAt = seasonWatch?.lastInProgressAt,
        )
    }

    // Dernier visionnage toutes saisons confondues — garde-fou « rewatch en cours »
    fun seriesLastWatchedAt(series: CleanupSeries): LocalDateTime? =
        seasonWatchesOf(series).second.maxOfOrNull { it.lastWatchedAt }

    private fun seasonWatchesOf(series: CleanupSeries): Pair<Correlation, List<SeasonWatchAggregate>> {
        val providerEntry = series.tvdbId?.toString()?.let { seriesEntriesByTvdb[it] }
            ?: series.imdbId?.let { seriesEntriesByImdb[it] }
        if (providerEntry != null) {
            return Correlation.PROVIDER_ID to seasonWatchesBySeriesId[providerEntry.itemId].orEmpty()
        }

        val titleEntry = uniqueEntryByTitle(seriesEntriesByTitle, series.title, series.year)
        if (titleEntry != null) {
            return Correlation.TITLE to seasonWatchesBySeriesId[titleEntry.itemId].orEmpty()
        }

        val watchesByName = seasonWatchesBySeriesName[Titles.normalize(series.title)].orEmpty()
        if (watchesByName.mapNotNull { it.seriesId }.distinct().size <= 1 && watchesByName.isNotEmpty()) {
            return Correlation.TITLE to watchesByName
        }

        return Correlation.NONE to emptyList()
    }

    // Match par titre normalisé, départagé par l'année (±1) ; plusieurs homonymes -> non corrélé
    private fun uniqueEntryByTitle(
        entriesByTitle: Map<String, List<JellyfinLibraryEntry>>,
        title: String,
        year: Int?,
    ): JellyfinLibraryEntry? {
        val entries = entriesByTitle[Titles.normalize(title)].orEmpty()
        if (entries.isEmpty()) return null
        if (entries.size == 1) return entries.single()
        if (year == null) return null
        return entries.singleOrNull { entry ->
            entry.productionYear != null && entry.productionYear in (year - 1)..(year + 1)
        }
    }

    private fun MovieWatchAggregate?.toCorrelated(correlation: Correlation) = CorrelatedWatch(
        correlation = correlation,
        lastWatchedAt = this?.lastWatchedAt,
        completedBySomeone = this?.completedBySomeone == true,
        startedBySomeone = this != null,
        lastInProgressAt = this?.lastInProgressAt,
    )

    private fun uncorrelated() = CorrelatedWatch(
        correlation = Correlation.NONE,
        lastWatchedAt = null,
        completedBySomeone = false,
        startedBySomeone = false,
        lastInProgressAt = null,
    )
}
