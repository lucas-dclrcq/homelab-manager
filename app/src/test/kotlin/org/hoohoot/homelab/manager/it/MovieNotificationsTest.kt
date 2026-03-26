package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.InjectSynapse
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.it.config.SynapseTestResource
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomProvider
import org.hoohoot.homelab.manager.notifications.resource.RadarrResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(RadarrResource::class)
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class MovieNotificationsTest {
    @InjectSynapse
    private val synapseTestClient: SynapseTestClient? = null

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var mediaRoomId: String

    @BeforeEach
    fun setUp() {
        mediaRoomId = synapseTestClient!!.createRoom("media-${System.nanoTime()}")
        roomProvider.media = mediaRoomId
    }

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

        val lastMessage = synapseTestClient!!.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "🎬 Movie Downloaded\nThe Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/\n👤 Requested by : lucasd"
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>🎬 Movie Downloaded</h1><p>The Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/<br>👤 Requested by : lucasd</p>"
        )
    }

    @Test
    fun `should handle missing movie object with unknown fields`() {
        val payload = """
            {
                "movieFile": { "quality": "720p" },
                "eventType": "Download"
            }
        """.trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("unknown (unknown) [720p]")
        assertThat(body).contains("Requested by : unknown")
    }

    @Test
    fun `should handle missing movieFile with unknown quality`() {
        val payload = """
            {
                "movie": {
                    "id": 1,
                    "title": "Avatar",
                    "year": 2009,
                    "imdbId": "tt0499549",
                    "tags": ["Adventure", "1 - jane_doe"]
                },
                "eventType": "Download"
            }
        """.trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("Avatar (2009) [unknown]")
        assertThat(body).contains("Requested by : jane_doe")
    }

    @Test
    fun `should handle missing tags with unknown requester`() {
        val payload = """
            {
                "movie": {
                    "id": 1,
                    "title": "Titanic",
                    "year": 1997,
                    "imdbId": "tt0120338"
                },
                "movieFile": { "quality": "4K" },
                "eventType": "Download"
            }
        """.trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("Titanic (1997) [4K]")
        assertThat(body).contains("Requested by : unknown")
    }

    @Test
    fun `should handle empty payload with all unknown fields`() {
        val payload = """{"eventType": "Download"}"""

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("unknown (unknown) [unknown]")
        assertThat(body).contains("Requested by : unknown")
    }

    @Test
    fun `should ignore non-Download event type`() {
        val payload = """
            {
                "movie": { "id": 1, "title": "Test" },
                "eventType": "Grab"
            }
        """.trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val messageCount = synapseTestClient!!.getMessageCount(mediaRoomId)
        assertThat(messageCount).isEqualTo(0)
    }
}
