package org.hoohoot.homelab.manager.notifications.api

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

private const val DEFAULT_VALUE = "unknown"

fun SonarrWebhookPayload.quality(): String = this.episodeFile
    ?.quality
    ?: DEFAULT_VALUE

fun SonarrWebhookPayload.seriesName(): String = this.series
    ?.title
    ?: DEFAULT_VALUE

fun SonarrWebhookPayload.seasonAndEpisodeNumber(): String = this.episodes
    ?.firstOrNull()
    ?.let { "S%02dE%02d".format(it.seasonNumber, it.episodeNumber) }
    ?: DEFAULT_VALUE

fun SonarrWebhookPayload.episodeName(): String = this.episodes
    ?.firstOrNull()
    ?.title
    ?: DEFAULT_VALUE

fun SonarrWebhookPayload.indexer(): String = this.release
    ?.indexer
    ?.replace(" (Prowlarr)", "")
    ?: DEFAULT_VALUE

fun SonarrWebhookPayload.imdbId(): String = this.series
    ?.imdbId
    ?: DEFAULT_VALUE

fun SonarrWebhookPayload.requester(): String = this.series?.tags
    ?.requester()
    ?: DEFAULT_VALUE
