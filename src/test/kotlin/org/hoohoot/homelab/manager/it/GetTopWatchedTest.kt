package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.ws.rs.core.Response
import org.hamcrest.Matchers.equalTo
import org.hoohoot.homelab.manager.infrastructure.api.resources.MediaInfoResource
import org.hoohoot.homelab.manager.it.config.InjectWireMock
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@QuarkusTest
@TestHTTPEndpoint(MediaInfoResource::class)
@QuarkusTestResource(WiremockTestResource::class)
internal class GetTopWatchedTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
    }

    @ParameterizedTest
    @CsvSource("7,last-week,LastWeek", "30,last-month,LastMonth", "365,last-year,LastYear")
    fun `should return last week top watched`(days: String, apiParameter: String, period: String) {
        wireMockServer!!
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostPopularByType")).withRequestBody(equalToJson("""
                    {
                      "days": "$days",
                      "type": "Series"
                    }
                """.trimIndent(), true, true))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("""
                              [
                                {
                                  "unique_viewers": "3",
                                  "latest_activity_date": "2025-03-03T16:33:24.711Z",
                                  "Name": "The White Lotus",
                                  "Id": "7cbf7d33855704b4991d90450c382f4d",
                                  "PrimaryImageHash": "dON,bV~n9G_1Kj9HD%NG.PNHIVV[ix-.oLxa9GV[njoe",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "7",
                                  "latest_activity_date": "2025-03-02T20:58:18.585Z",
                                  "Name": "Severance",
                                  "Id": "2476d555ebfd0ac55ecb878839b0b4a4",
                                  "PrimaryImageHash": "drIh?H%NIVM{~qt7x[xa-;ofj@t7bbV@RjofWBIUj[xu",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "5",
                                  "latest_activity_date": "2025-02-27T21:12:01.538Z",
                                  "Name": "Bref",
                                  "Id": "cea25dbf13fb88bd598e5e42330b9016",
                                  "PrimaryImageHash": "d64o1d-;IUIUt7ofRjWB00D%%M%MD%Rjxut7_3%MM{IU",
                                  "archived": false
                                }
                              ]
                        """.trimIndent())
                        .withHeader("Content-Type", "application/json")
                    )
            )

        wireMockServer
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostPopularByType")).withRequestBody(equalToJson("""
                    {
                      "days": "$days",
                      "type": "Movie"
                    }
                """.trimIndent(), true, true))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("""
                              [
                                  {
                                    "unique_viewers": "5",
                                    "latest_activity_date": "2025-02-03T21:01:47.338Z",
                                    "Name": "Le Comte de Monte-Cristo",
                                    "Id": "4314efd1ef4667cd3dc892818f765d12",
                                    "PrimaryImageHash": "dB8WmZ%057fk?Gs:IpR+0gR,xtNG57WW-oWB${'$'}~j?xa%1",
                                    "archived": false
                                  },
                                  {
                                    "unique_viewers": "7",
                                    "latest_activity_date": "2025-03-16T14:47:17.533Z",
                                    "Name": "Le Seigneur des anneaux : La Communauté de l'anneau",
                                    "Id": "77df8e301917d0f056668007052d28a6",
                                    "PrimaryImageHash": "dLGk:caK%2?F8|M{M|Rj58t7IVW=t5x[WXj?02Rjf8V@",
                                    "archived": false
                                  }
                              ]
                        """.trimIndent())
                        .withHeader("Content-Type", "application/json")
                    )
            )

        wireMockServer
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostViewedByType")).withRequestBody(equalToJson("""
                    {
                      "days": "$days",
                      "type": "Series"
                    }
                """.trimIndent(), true, true))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("""
                              [
                                {
                                  "Plays": "249",
                                  "total_playback_duration": "282139",
                                  "Name": "Malcolm",
                                  "Id": "975785a867ee3eff1b1e2219a6a15b84",
                                  "PrimaryImageHash": "dtQcC*%M?^?voxV@xvtS-:j?M|og?HkCbbt7OsWCwvt7",
                                  "archived": false
                                },
                                {
                                  "Plays": "110",
                                  "total_playback_duration": "31821",
                                  "Name": "Simon",
                                  "Id": "e8e82cef90a1bb4867269cd68e16b8f7",
                                  "PrimaryImageHash": "dJStExtEmx]Ouj?WER:19S5t8R:wjbt4t1jujsnza#",
                                  "archived": false
                                }
                              ]
                        """.trimIndent())
                        .withHeader("Content-Type", "application/json")
                    )
            )

        wireMockServer
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostViewedByType")).withRequestBody(equalToJson("""
                    {
                      "days": "$days",
                      "type": "Movie"
                    }
                """.trimIndent(), true, true))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("""
                              [
                                {
                                  "Plays": "6",
                                  "total_playback_duration": "24236",
                                  "Name": "Vaiana 2",
                                  "Id": "46f3c145ed1c8b56c8c4b83a706d982c",
                                  "PrimaryImageHash": "dPEou}XTo~%hTn2ngV[0j%3RPV[tmtSt6oyrBRjxsM{",
                                  "archived": false
                                },
                                {
                                  "Plays": "5",
                                  "total_playback_duration": "6345",
                                  "Name": "Oscar",
                                  "Id": "5e932ef1600b0bf031e57b20bef899ef",
                                  "PrimaryImageHash": "ddQ05R^+_N?u~qo3NGx]t7IoayxCs-t7%NWB%gtRM|kD",
                                  "archived": false
                                }
                              ]
                        """.trimIndent())
                        .withHeader("Content-Type", "application/json")
                    )
            )

        RestAssured.given()
            .`when`().get("/top-watched/$apiParameter")
            .then().statusCode(Response.Status.OK.statusCode)
            .body("period", equalTo(period))
            .body("mostPopularSeries[0].name", equalTo("Severance"))
            .body("mostPopularSeries[0].uniqueViewers", equalTo(7))
            .body("mostPopularSeries[1].name", equalTo("Bref"))
            .body("mostPopularSeries[1].uniqueViewers", equalTo(5))
            .body("mostPopularSeries[2].name", equalTo("The White Lotus"))
            .body("mostPopularSeries[2].uniqueViewers", equalTo(3))
            .body("mostPopularMovies[0].name", equalTo("Le Seigneur des anneaux : La Communauté de l'anneau"))
            .body("mostPopularMovies[0].uniqueViewers", equalTo(7))
            .body("mostPopularMovies[1].name", equalTo("Le Comte de Monte-Cristo"))
            .body("mostPopularMovies[1].uniqueViewers", equalTo(5))
            .body("mostViewedSeries[0].name", equalTo("Malcolm"))
            .body("mostViewedSeries[0].plays", equalTo(249))
            .body("mostViewedSeries[0].totalPlaybackInHours", equalTo("78h"))
            .body("mostViewedSeries[1].name", equalTo("Simon"))
            .body("mostViewedSeries[1].plays", equalTo(110))
            .body("mostViewedSeries[1].totalPlaybackInHours", equalTo("9h"))
            .body("mostViewedMovies[0].name", equalTo("Vaiana 2"))
            .body("mostViewedMovies[0].plays", equalTo(6))
            .body("mostViewedMovies[0].totalPlaybackInHours", equalTo("7h"))
            .body("mostViewedMovies[1].name", equalTo("Oscar"))
            .body("mostViewedMovies[1].plays", equalTo(5))
            .body("mostViewedMovies[1].totalPlaybackInHours", equalTo("2h"))
    }
}