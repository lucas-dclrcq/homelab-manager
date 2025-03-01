package org.hoohoot.homelab.manager.notifications.application.ports.arr

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