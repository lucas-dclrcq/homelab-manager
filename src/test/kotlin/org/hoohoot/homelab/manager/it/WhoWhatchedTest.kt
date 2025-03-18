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
import org.hoohoot.homelab.manager.infrastructure.api.resources.NotificationsResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource::class)
@QuarkusTestResource(WiremockTestResource::class)
internal class WhoWhatchedTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
    }

    @Test
    fun `should return who watched series info`() {
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
    }
}