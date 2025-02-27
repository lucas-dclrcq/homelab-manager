package org.hoohoot.homelab.manager.notifications.domain

import io.vertx.core.json.JsonObject

private const val DEFAULT_VALUE = "unknown"

data class Movie(val title: String, val year: String, val imdbId: String, val quality: String, val requester: String)

class ParseMovie private constructor(private val payload : JsonObject) {

    companion object {
        @JvmStatic
        fun from(payload: JsonObject): Movie {
            val parseMovie = ParseMovie(payload)
            return Movie(parseMovie.title(), parseMovie.year(), parseMovie.imdbId(), parseMovie.quality(), parseMovie.requester())
        }
    }

    private fun movie() = payload.getJsonObject("movie")

    private fun movieFile() = payload.getJsonObject("movieFile")

    private fun title() = movie()?.getString("title") ?: DEFAULT_VALUE

    private fun year() = movie()?.getString("year") ?: DEFAULT_VALUE

    private fun imdbId() = movie()?.getString("imdbId") ?: DEFAULT_VALUE

    private fun quality() = movieFile()?.getString("quality") ?: DEFAULT_VALUE

    private fun requester() = movie()?.getJsonArray("tags")?.requester() ?: DEFAULT_VALUE

}