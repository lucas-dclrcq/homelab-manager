package org.hoohoot.homelab.manager.notifications.arr.sonarr

data class SonarrWebhookPayload(
    val eventType: String? = null,
    val series: SonarrWebhookSeries? = null,
    val episodes: List<SonarrWebhookEpisode>? = null,
    val episodeFile: SonarrWebhookEpisodeFile? = null,
    val release: SonarrWebhookRelease? = null,
    val downloadClient: String? = null,
)

data class SonarrWebhookSeries(
    val id: Int? = null,
    val title: String? = null,
    val year: Int? = null,
    val imdbId: String? = null,
    val tags: List<String>? = null,
)

data class SonarrWebhookEpisode(
    val episodeNumber: Int? = null,
    val seasonNumber: Int? = null,
    val title: String? = null,
)

data class SonarrWebhookEpisodeFile(
    val quality: String? = null,
)

data class SonarrWebhookRelease(
    val indexer: String? = null,
)
