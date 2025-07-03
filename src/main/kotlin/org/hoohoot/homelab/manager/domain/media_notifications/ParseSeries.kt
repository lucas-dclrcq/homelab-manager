package org.hoohoot.homelab.manager.domain.media_notifications

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

private const val DEFAULT_VALUE = "unknown"

data class Series(
    val imdbId: String,
    val indexer: String,
    val quality: String,
    val seriesName: String,
    val seasonAndEpisodeNumber: String,
    val episodeName: String,
    val downloadClient: String,
    val requester: String)

class ParseSeries private constructor(payload: JsonObject) {
    private val series: JsonObject? = payload.getJsonObject("series")
    private val episodes: JsonArray? = payload.getJsonArray("episodes")
    private val episodeFile: JsonObject? = payload.getJsonObject("episodeFile")
    private val release: JsonObject? = payload.getJsonObject("release")
    private val downloadClient: String? = payload.getString("downloadClient")

    companion object {
        @JvmStatic
        fun from(payload: JsonObject): Series {
            val parseSeries = ParseSeries(payload)
            return Series(parseSeries.imdbId(), parseSeries.indexer(), parseSeries.quality(), parseSeries.seriesName(), parseSeries.seasonAndEpisodeNumber(), parseSeries.episodeName(), parseSeries.downloadClient(), parseSeries.requester())
        }
    }

    private fun quality(): String = episodeFile
        ?.getString("quality")
        ?: DEFAULT_VALUE

    private fun seriesName(): String = series
        ?.getString("title")
        ?: DEFAULT_VALUE

    private fun seasonAndEpisodeNumber(): String = episodes
        ?.getJsonObject(0)
        ?.let {
            val episodeNumber = it.getInteger("episodeNumber")
            val seasonNumber = it.getInteger("seasonNumber")
            "S%02dE%02d".format(seasonNumber, episodeNumber)
        }
        ?: DEFAULT_VALUE

    private fun episodeName(): String = episodes
        ?.getJsonObject(0)
        ?.getString("title")
        ?: DEFAULT_VALUE

    private fun downloadClient(): String = downloadClient ?: DEFAULT_VALUE

    private fun indexer(): String = release
        ?.getString("indexer")
        ?.replace(" (Prowlarr)", "")
        ?: DEFAULT_VALUE

    private fun imdbId(): String = series
        ?.getString("imdbId")
        ?: DEFAULT_VALUE

    private fun requester(): String = series?.getJsonArray("tags")
        ?.requester()
        ?: DEFAULT_VALUE
}