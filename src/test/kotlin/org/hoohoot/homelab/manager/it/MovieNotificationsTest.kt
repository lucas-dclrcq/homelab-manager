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
import org.hoohoot.homelab.manager.notifications.resource.RadarrResource
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(RadarrResource::class)
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class MovieNotificationsTest {
    @InjectSynapse
    private val synapseClient: SynapseClient? = null

    private val notification = """
        {
          "movie": {
            "id": 686,
            "title": "The Wild Robot",
            "year": 2024,
            "releaseDate": "yyyy-MM-dd",
            "folderPath": "<pathToFolder>",
            "tmdbId": 1,
            "imdbId": "tt29623480",
            "tags": [
              "1 - lucasd"
            ]
          },
          "remoteMovie": {
            "tmdbId": 1,
            "imdbId": "tt<id>",
            "title": "<movieTitle>",
            "year": 1
          },
          "movieFile": {
            "id": 36745,
            "relativePath": "<filenameAfterRename",
            "path": "<pathToDownloadedFile>",
            "quality": "WEBDL-720p",
            "qualityVersion": 1,
            "releaseGroup": "<rlsGroup>",
            "sceneName": "The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>",
            "indexerFlags": "0",
            "size": 1
          },
          "isUpgrade": false,
          "downloadClient": "<downloadClient>",
          "downloadClientType": "<type>",
          "downloadId": "<downloadClient>_The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>_<longInt>",
          "eventType": "Download"
        }
    """.trimIndent()

    @Test
    fun `should send movie downloaded notification`() {
        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient!!.getLastMessage(synapseClient.roomId("media"))
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "🎬 Movie Downloaded\nThe Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/\n👤 Requested by : lucasd"
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>🎬 Movie Downloaded</h1><p>The Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/<br>👤 Requested by : lucasd</p>"
        )
    }

    @Test
    fun `should send notification to configured room`() {
        val messageCountBefore = synapseClient!!.getMessageCount(synapseClient.roomId("media"))

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val messageCountAfter = synapseClient.getMessageCount(synapseClient.roomId("media"))
        assertThat(messageCountAfter).isGreaterThan(messageCountBefore)
    }
}
