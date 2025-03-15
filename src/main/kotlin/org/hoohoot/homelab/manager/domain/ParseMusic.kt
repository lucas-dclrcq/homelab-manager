package org.hoohoot.homelab.manager.domain

import io.vertx.core.json.JsonObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val DEFAULT_VALUE = "unknown"

data class Album(val downloadClient: String, val artistName: String, val albumTitle: String, val coverUrl: String, val genres: List<String>, val year: String)

class ParseMusic private constructor(private val payload : JsonObject) {

    companion object {
        @JvmStatic
        fun from(payload: JsonObject): Album {
            val parseMusic = ParseMusic(payload)
            return Album(parseMusic.downloadClient(), parseMusic.artistName(), parseMusic.albumTitle(), parseMusic.coverUrl(), parseMusic.genres(), parseMusic.year())
        }
    }

    private fun album() = payload.getJsonObject("album")

    private fun albumTitle() = album()?.getString("title") ?: DEFAULT_VALUE

    private fun artistName() = payload.getJsonObject("artist")?.getString("name") ?: DEFAULT_VALUE

    private fun coverUrl() = album()
        ?.getJsonArray("images")
        ?.map { o: Any -> o as JsonObject }
        ?.filter { image: JsonObject -> "cover" == image.getString("coverType") }
        ?.map { image: JsonObject -> image.getString("remoteUrl") }
        ?.firstOrNull()
        ?: DEFAULT_VALUE

    private fun genres() = album()
        ?.getJsonArray("genres")
        ?.map { it as String }
        ?: emptyList()

    private fun year() = album()
        ?.getString("releaseDate")
        ?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }.getOrNull() }
        ?.let { DateTimeFormatter.ofPattern("yyyy").format(it) }
        ?: DEFAULT_VALUE

    private fun downloadClient() = payload.getString("downloadClient") ?: DEFAULT_VALUE

}