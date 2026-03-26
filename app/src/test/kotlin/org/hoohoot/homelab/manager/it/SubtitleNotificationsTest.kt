package org.hoohoot.homelab.manager.it

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class SubtitleNotificationsTest {
    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var mediaRoomId: String

    @BeforeEach
    fun setUp() {
        mediaRoomId = synapseTestClient.createRoom("media-${System.nanoTime()}")
        roomProvider.media = mediaRoomId
    }

    private fun bazarrNotification(message: String) = """
        {
            "title": "Bazarr",
            "message": "$message",
            "type": "info"
        }
    """.trimIndent()

    private fun radarrNotification(movieId: Int, title: String, year: Int) = """
        {
          "movie": {
            "id": $movieId,
            "title": "$title",
            "year": $year,
            "imdbId": "tt0000001",
            "tags": ["1 - lucasd"]
          },
          "movieFile": { "quality": "WEBDL-1080p" },
          "eventType": "Download"
        }
    """.trimIndent()

    private fun sonarrNotification(seriesId: Int, title: String, year: Int, episodeNumber: Int) = """
        {
          "series": {
            "id": $seriesId,
            "title": "$title",
            "year": $year,
            "imdbId": "tt0000002",
            "tags": ["11 - flo"]
          },
          "episodes": [{ "episodeNumber": $episodeNumber, "seasonNumber": 1, "title": "Episode $episodeNumber" }],
          "episodeFile": { "quality": "WEBDL-720p" },
          "release": { "indexer": "NZBFinder (Prowlarr)" },
          "downloadClient": "SABnzbd",
          "eventType": "Download"
        }
    """.trimIndent()

    @Test
    fun `should thread movie subtitle notification under movie download notification`() {
        // 1. Radarr notifies movie downloaded
        RestAssured.given().contentType(ContentType.JSON)
            .body(radarrNotification(movieId = 900, title = "Interstellar", year = 2014))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/radarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val movieEventId = synapseTestClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()

        // 2. Bazarr notifies subtitle downloaded for same movie
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Interstellar (2014) : French subtitles downloaded from opensubtitles with a score of 95%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(movieEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
        assertThat(lastMessage.get("body").asText()).contains("💬 Subtitle Downloaded")
        assertThat(lastMessage.get("body").asText()).contains("French")
    }

    @Test
    fun `should thread series subtitle notification under episode download notification`() {
        // 1. Sonarr notifies episode downloaded
        RestAssured.given().contentType(ContentType.JSON)
            .body(sonarrNotification(seriesId = 901, title = "Breaking Bad", year = 2008, episodeNumber = 1))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/sonarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val seriesEventId = synapseTestClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()

        // 2. Bazarr notifies subtitle downloaded for same series
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Breaking Bad (2008) - S01E01 - Pilot : French subtitles downloaded from opensubtitles with a score of 90%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(seriesEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
        assertThat(lastMessage.get("body").asText()).contains("💬 Subtitle Downloaded")
        assertThat(lastMessage.get("body").asText()).contains("French")
    }

    @Test
    fun `should send standalone subtitle notification when no matching media exists`() {
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Unknown Movie (1999) : English subtitles downloaded from subscene with a score of 80%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to")).isNull()
        assertThat(lastMessage.get("body").asText()).contains("💬 Subtitle Downloaded")
        assertThat(lastMessage.get("body").asText()).contains("Unknown Movie (1999)")
    }

    @Test
    fun `should handle upgraded subtitle action`() {
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Inception (2010) : French subtitles upgraded from addic7ed with a score of 98%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("French subtitles upgraded from addic7ed (score: 98%)")
    }

    @Test
    fun `should handle manually downloaded subtitle action`() {
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Inception (2010) : French subtitles manually downloaded from subscene with a score of 85%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("French subtitles manually downloaded from subscene (score: 85%)")
    }

    @Test
    fun `should handle HI subtitle language`() {
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Inception (2010) : English (HI) subtitles downloaded from opensubtitles with a score of 92%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("English (HI) subtitles downloaded")
    }

    @Test
    fun `should handle forced subtitle language`() {
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Inception (2010) : French (forced) subtitles downloaded from opensubtitles with a score of 80%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("French (forced) subtitles downloaded")
    }

    @Test
    fun `should return server error on null message`() {
        val payload = """{"title": "Bazarr", "type": "info"}"""

        RestAssured.given().contentType(ContentType.JSON)
            .body(payload)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
    }

    @Test
    fun `should return server error on unparseable message`() {
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("some random text"))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/bazarr")
            .then().statusCode(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
    }
}
