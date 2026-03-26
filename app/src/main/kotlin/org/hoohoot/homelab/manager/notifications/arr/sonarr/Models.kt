package org.hoohoot.homelab.manager.notifications.arr.sonarr

data class Episode(
    val seriesId: Int? = null,
    val tvdbId: Int? = null,
    val episodeFileId: Int? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val title: String? = null,
    val airDate: String? = null,
    val airDateUtc: String? = null,
    val runtime: Int? = null,
    val hasFile: Boolean? = null,
    val monitored: Boolean? = null,
    val absoluteEpisodeNumber: Int? = null,
    val unverifiedSceneNumbering: Boolean? = null,
    val series: Series? = null,
    val id: Int? = null
)

data class Series(
    val title: String? = null,
    val sortTitle: String? = null,
    val status: String? = null,
    val ended: Boolean? = null,
    val overview: String? = null,
    val network: String? = null,
    val airTime: String? = null,
    val images: List<Image>? = null,
    val originalLanguage: Language? = null,
    val seasons: List<Season>? = null,
    val year: Int? = null,
    val path: String? = null,
    val qualityProfileId: Int? = null,
    val seasonFolder: Boolean? = null,
    val monitored: Boolean? = null,
    val monitorNewItems: String? = null,
    val useSceneNumbering: Boolean? = null,
    val runtime: Int? = null,
    val tvdbId: Int? = null,
    val tvRageId: Int? = null,
    val tvMazeId: Int? = null,
    val tmdbId: Int? = null,
    val firstAired: String? = null,
    val lastAired: String? = null,
    val seriesType: String? = null,
    val cleanTitle: String? = null,
    val imdbId: String? = null,
    val titleSlug: String? = null,
    val genres: List<String>? = null,
    val tags: List<Int>? = null,
    val added: String? = null,
    val ratings: Rating? = null,
    val languageProfileId: Int? = null,
    val id: Int? = null
)

data class Image(
    val coverType: String? = null,
    val remoteUrl: String? = null
)

data class Language(
    val id: Int? = null,
    val name: String? = null
)

data class Rating(
    val votes: Int? = null,
    val value: Double? = null
)

data class Season(
    val seasonNumber: Int? = null,
    val monitored: Boolean? = null
)
