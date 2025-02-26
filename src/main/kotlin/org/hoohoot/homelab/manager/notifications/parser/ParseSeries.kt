package org.hoohoot.homelab.manager.notifications.parser

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

private const val DEFAULT_VALUE = "unknown"

class ParseSeries private constructor(payload: JsonObject) {
    private val series: JsonObject? = payload.getJsonObject("series")
    private val episodes: JsonArray? = payload.getJsonArray("episodes")
    private val episodeFiles: JsonArray? = payload.getJsonArray("episodeFiles")
    private val release: JsonObject? = payload.getJsonObject("release")
    private val downloadClient: String? = payload.getString("downloadClient")

    companion object {
        @JvmStatic
        fun from(payload: JsonObject) = ParseSeries(payload)
    }

    fun quality(): String = episodeFiles
        ?.getJsonObject(0)
        ?.getString("quality")
        ?: DEFAULT_VALUE

    fun seriesName(): String = series
        ?.getString("title")
        ?: DEFAULT_VALUE

    fun seasonAndEpisodeNumber(): String = episodes
        ?.getJsonObject(0)
        ?.let {
            val episodeNumber = it.getInteger("episodeNumber")
            val seasonNumber = it.getInteger("seasonNumber")
            "S%02dE%02d".format(seasonNumber, episodeNumber)
        }
        ?: DEFAULT_VALUE

    fun episodeName(): String = episodes
        ?.getJsonObject(0)
        ?.getString("title")
        ?: DEFAULT_VALUE

    fun downloadClient(): String = downloadClient ?: DEFAULT_VALUE

    fun indexer(): String = release
        ?.getString("indexer")
        ?.replace(" (Prowlarr)", "")
        ?: DEFAULT_VALUE

    fun imdbLink(): String = series
        ?.getString("imdbId")
        ?.let { "https://www.imdb.com/title/$it/" }
        ?: DEFAULT_VALUE

    fun requester(): String = series?.getJsonArray("tags")
        ?.requester()
        ?: DEFAULT_VALUE
}