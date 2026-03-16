package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.InjectSynapse
import org.hoohoot.homelab.manager.it.config.SynapseClient
import org.hoohoot.homelab.manager.it.config.SynapseTestResource
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.resource.NotificationsResource
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource::class)
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class SubtitleNotificationsTest {
    @InjectSynapse
    private val synapseClient: SynapseClient? = null

    private fun bazarrNotification(body: String) = """
        {
            "title": "Bazarr",
            "body": "$body",
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
        val mediaRoomId = synapseClient!!.roomId("media")

        // 1. Radarr notifies movie downloaded
        RestAssured.given().contentType(ContentType.JSON)
            .body(radarrNotification(movieId = 900, title = "Interstellar", year = 2014))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/radarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val movieEventId = synapseClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()

        // 2. Bazarr notifies subtitle downloaded for same movie
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Interstellar (2014) : French subtitles downloaded from opensubtitles with a score of 95%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(movieEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
        assertThat(lastMessage.get("body").asText()).contains("Subtitle Downloaded")
        assertThat(lastMessage.get("body").asText()).contains("French")
    }

    @Test
    fun `should thread series subtitle notification under episode download notification`() {
        val mediaRoomId = synapseClient!!.roomId("media")

        // 1. Sonarr notifies episode downloaded
        RestAssured.given().contentType(ContentType.JSON)
            .body(sonarrNotification(seriesId = 901, title = "Breaking Bad", year = 2008, episodeNumber = 1))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/sonarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val seriesEventId = synapseClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()

        // 2. Bazarr notifies subtitle downloaded for same series
        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Breaking Bad (2008) - S01E01 - Pilot : French subtitles downloaded from opensubtitles with a score of 90%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(seriesEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
        assertThat(lastMessage.get("body").asText()).contains("Subtitle Downloaded")
        assertThat(lastMessage.get("body").asText()).contains("French")
    }

    @Test
    fun `should send standalone subtitle notification when no matching media exists`() {
        val mediaRoomId = synapseClient!!.roomId("media")

        RestAssured.given().contentType(ContentType.JSON)
            .body(bazarrNotification("Unknown Movie (1999) : English subtitles downloaded from subscene with a score of 80%."))
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/bazarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to")).isNull()
        assertThat(lastMessage.get("body").asText()).contains("Subtitle Downloaded")
        assertThat(lastMessage.get("body").asText()).contains("Unknown Movie (1999)")
    }
}
