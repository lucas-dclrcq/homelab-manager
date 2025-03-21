package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.hamcrest.Matchers.equalTo
import org.hoohoot.homelab.manager.infrastructure.api.resources.MediaInfoResource
import org.hoohoot.homelab.manager.it.config.InjectWireMock
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.infrastructure.api.resources.NotificationsResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(MediaInfoResource::class)
@QuarkusTestResource(WiremockTestResource::class)
internal class WhoWatchedTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
    }

    @Test
    fun `should return who watched series info`() {
        val jellyfinPayload = """
            {
              "SearchHints": [
                {
                  "ItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Id": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Name": "Severance",
                  "ProductionYear": 2022,
                  "PrimaryImageTag": "24b129c0824e6c96fc2cfa95db0e1b17",
                  "ThumbImageTag": "dbb3120176e7709d20c853d5845f1c08",
                  "ThumbImageItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "BackdropImageTag": "0c9e0a798319c1f18c5b9b85b701ab0b",
                  "BackdropImageItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Type": "Series",
                  "IsFolder": true,
                  "RunTimeTicks": 0,
                  "MediaType": "Unknown",
                  "EndDate": "2025-03-13T00:00:00.0000000Z",
                  "Status": "Continuing",
                  "Artists": [],
                  "ChannelId": null,
                  "PrimaryImageAspectRatio": 0.68
                }
              ],
              "TotalRecordCount": 1
            }
        """.trimIndent()

        val jellystatPayload = """
            {
              "current_page": 1,
              "pages": 1,
              "size": 50,
              "sort": "ActivityDateInserted",
              "desc": true,
              "results": [
                {
                  "UserName": "jacquesdurand",
                  "EpisodeNumber": 1,
                  "SeasonNumber": 2,
                  "FullName": "Severance : S2E1 - Hello, Ms. Cobel"
                },
                {
                  "UserName": "michel",
                  "EpisodeNumber": 3,
                  "SeasonNumber": 2,
                  "FullName": "Severance : S2E3 - Hello, Michel"
                }
              ]
            }
        """.trimIndent()

        wireMockServer!!
            .stubFor(
                WireMock.get(WireMock.urlPathMatching("/Search/Hints"))
                    .willReturn(
                        WireMock.aResponse().withStatus(200)
                        .withBody(jellyfinPayload)
                        .withHeader("Content-Type", "application/json")
                    )
            )

        wireMockServer
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/api/getItemHistory"))
                    .willReturn(
                        WireMock.aResponse().withStatus(200)
                            .withBody(jellystatPayload)
                            .withHeader("Content-Type", "application/json")
                    )
            )

        RestAssured.given()
            .queryParam("searchTerm", "Severance")
            .`when`().get("/who-watched")
            .then().statusCode(Response.Status.OK.statusCode)
            .body("tvShow", equalTo("Severance"))
            .body("watchersCount", equalTo(2))
            .body("watchers[1].username", equalTo("jacquesdurand"))
            .body("watchers[1].episodeWatchedCount", equalTo(1))
            .body("watchers[1].lastEpisodeWatched", equalTo("Severance : S2E1 - Hello, Ms. Cobel"))
            .body("watchers[1].seasonNumber", equalTo(2))
            .body("watchers[1].episodeNumber", equalTo(1))
            .body("watchers[0].username", equalTo("michel"))
            .body("watchers[0].episodeWatchedCount", equalTo(1))
            .body("watchers[0].lastEpisodeWatched", equalTo("Severance : S2E3 - Hello, Michel"))
            .body("watchers[0].seasonNumber", equalTo(2))
            .body("watchers[0].episodeNumber", equalTo(3))

    }

    @Test
    fun `should return internal server error when multiple series are found for search term`() {
        val jellyfinPayload = """
            {
              "SearchHints": [
                {
                  "ItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Id": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Name": "Severance",
                  "ProductionYear": 2022,
                  "PrimaryImageTag": "24b129c0824e6c96fc2cfa95db0e1b17",
                  "ThumbImageTag": "dbb3120176e7709d20c853d5845f1c08",
                  "ThumbImageItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "BackdropImageTag": "0c9e0a798319c1f18c5b9b85b701ab0b",
                  "BackdropImageItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Type": "Series",
                  "IsFolder": true,
                  "RunTimeTicks": 0,
                  "MediaType": "Unknown",
                  "EndDate": "2025-03-13T00:00:00.0000000Z",
                  "Status": "Continuing",
                  "Artists": [],
                  "ChannelId": null,
                  "PrimaryImageAspectRatio": 0.68
                },
                {
                  "ItemId": "3476d555ebfd0ac55ecb878839b0b4a4",
                  "Id": "3476d555ebfd0ac55ecb878839b0b4a4",
                  "Name": "Severance 2",
                  "ProductionYear": 2022,
                  "PrimaryImageTag": "24b129c0824e6c96fc2cfa95db0e1b17",
                  "ThumbImageTag": "dbb3120176e7709d20c853d5845f1c08",
                  "ThumbImageItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "BackdropImageTag": "0c9e0a798319c1f18c5b9b85b701ab0b",
                  "BackdropImageItemId": "2476d555ebfd0ac55ecb878839b0b4a4",
                  "Type": "Series",
                  "IsFolder": true,
                  "RunTimeTicks": 0,
                  "MediaType": "Unknown",
                  "EndDate": "2025-03-13T00:00:00.0000000Z",
                  "Status": "Continuing",
                  "Artists": [],
                  "ChannelId": null,
                  "PrimaryImageAspectRatio": 0.68
                }
              ],
              "TotalRecordCount": 1
            }
        """.trimIndent()

        wireMockServer!!
            .stubFor(
                WireMock.get(WireMock.urlPathMatching("/Search/Hints"))
                    .willReturn(
                        WireMock.aResponse().withStatus(200)
                            .withBody(jellyfinPayload)
                            .withHeader("Content-Type", "application/json")
                    )
            )

        RestAssured.given()
            .queryParam("searchTerm", "Severance")
            .`when`().get("/who-watched")
            .then().statusCode(Response.Status.INTERNAL_SERVER_ERROR.statusCode)
    }

    @Test
    fun `should return bad request when no search parameter is set`() {
              RestAssured.given()
            .`when`().get("/who-watched")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }
}