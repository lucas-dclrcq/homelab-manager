package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.awaitility.Awaitility
import org.hoohoot.homelab.manager.it.config.InjectWireMock
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.infrastructure.api.NotificationsResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource::class)
@QuarkusTestResource(WiremockTestResource::class)
internal class MovieNotificationsTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
        wireMockServer
            .stubFor(
                WireMock.put(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                    .willReturn(WireMock.aResponse().withStatus(200))
            )
    }

    @Test
    fun `should send movie downloaded notification`() {
        val notification = """
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

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/radarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype" : "m.text",
                              "body" : "Movie Downloaded\nThe Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/\nRequested by : lucasd",
                              "format" : "org.matrix.custom.html",
                              "formatted_body" : "<h1>Movie Downloaded</h1><p>The Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/<br>Requested by : lucasd</p>",
                              "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send notification to configured room`() {
        val notification = """
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
                    "quality": "WEBDL-1080p",
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

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/radarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/!media:test-server.tld/send/m.room.message/.*"))
        )
    }

    @Test
    fun `should use uuid as random transaction id`() {
        val notification = """
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
                    "quality": "WEBDL-1080p",
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

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/radarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
        )
    }

    @Test
    fun `should add matrix token as bearer`() {
        val notification = """
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
                    "quality": "WEBDL-1080p",
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

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/radarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withHeader("Authorization", WireMock.equalTo("Bearer TOKEN"))
        )
    }
}