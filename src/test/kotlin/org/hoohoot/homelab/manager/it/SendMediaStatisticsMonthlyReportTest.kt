package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.hoohoot.homelab.manager.it.config.InjectWireMock
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.infrastructure.api.NotificationsResource
import org.hoohoot.homelab.manager.notifications.infrastructure.time.TimeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource::class)
@QuarkusTestResource(WiremockTestResource::class)
class SendMediaStatisticsMonthlyReportTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
        wireMockServer
            .stubFor(
                WireMock.put(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"event_id\": \"toto\"}"))
            )
    }

    @Test
    fun `should send media statistics monthly report`() {
        wireMockServer!!
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostPopularByType"))
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
                                  "unique_viewers": "7",
                                  "latest_activity_date": "2025-02-27T21:12:01.538Z",
                                  "Name": "Bref",
                                  "Id": "cea25dbf13fb88bd598e5e42330b9016",
                                  "PrimaryImageHash": "d64o1d-;IUIUt7ofRjWB00D%%M%MD%Rjxut7_3%MM{IU",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "3",
                                  "latest_activity_date": "2025-03-03T07:26:58.021Z",
                                  "Name": "Malcolm",
                                  "Id": "975785a867ee3eff1b1e2219a6a15b84",
                                  "PrimaryImageHash": "dtQcC*%M?^?voxV@xvtS-:j?M|og?HkCbbt7OsWCwvt7",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "2",
                                  "latest_activity_date": "2025-02-25T12:19:06.907Z",
                                  "Name": "Zero Day",
                                  "Id": "3d6f8ded70a1c0cddeae95cd0f2e660f",
                                  "PrimaryImageHash": "dFEVc}~CE19]Hq-UIo0K^%V?R-tR-:aeR+xu9Ft7bFIU",
                                  "archived": false
                                }
                              ]
                        """.trimIndent())
                        .withHeader("Content-Type", "application/json")
                    )
            )

        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-monthly-top-watched-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype" : "m.text",
                              "body" : "Top of the Month\n\nMost popular series\n1. Severance : 7 viewers\n2. Bref : 7 viewers\n3. The White Lotus : 3 viewers\n4. Malcolm : 3 viewers\n5. Zero Day : 2 viewers",
                              "format" : "org.matrix.custom.html",
                              "formatted_body" : "<h1>Top of the Month</h1><p><br><b>Most popular series</b><br>1. Severance : 7 viewers<br>2. Bref : 7 viewers<br>3. The White Lotus : 3 viewers<br>4. Malcolm : 3 viewers<br>5. Zero Day : 2 viewers</p>",
                              "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send media statistics monthly report report to correct room`() {
        wireMockServer!!
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostPopularByType"))
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
                                  "unique_viewers": "7",
                                  "latest_activity_date": "2025-02-27T21:12:01.538Z",
                                  "Name": "Bref",
                                  "Id": "cea25dbf13fb88bd598e5e42330b9016",
                                  "PrimaryImageHash": "d64o1d-;IUIUt7ofRjWB00D%%M%MD%Rjxut7_3%MM{IU",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "3",
                                  "latest_activity_date": "2025-03-03T07:26:58.021Z",
                                  "Name": "Malcolm",
                                  "Id": "975785a867ee3eff1b1e2219a6a15b84",
                                  "PrimaryImageHash": "dtQcC*%M?^?voxV@xvtS-:j?M|og?HkCbbt7OsWCwvt7",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "2",
                                  "latest_activity_date": "2025-02-25T12:19:06.907Z",
                                  "Name": "Zero Day",
                                  "Id": "3d6f8ded70a1c0cddeae95cd0f2e660f",
                                  "PrimaryImageHash": "dFEVc}~CE19]Hq-UIo0K^%V?R-tR-:aeR+xu9Ft7bFIU",
                                  "archived": false
                                }
                              ]
                        """.trimIndent())
                        .withHeader("Content-Type", "application/json")
                    )
            )

        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-monthly-top-watched-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/!media:test-server.tld/send/m.room.message/.*"))
        )

    }

    @Test
    fun `should fetch series statistics using x-api-token`() {
        wireMockServer!!
            .stubFor(
                WireMock.post(WireMock.urlPathMatching("/stats/getMostPopularByType"))
                    .willReturn(
                        WireMock.aResponse().withStatus(200)
                            .withBody(
                                """
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
                                  "unique_viewers": "7",
                                  "latest_activity_date": "2025-02-27T21:12:01.538Z",
                                  "Name": "Bref",
                                  "Id": "cea25dbf13fb88bd598e5e42330b9016",
                                  "PrimaryImageHash": "d64o1d-;IUIUt7ofRjWB00D%%M%MD%Rjxut7_3%MM{IU",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "3",
                                  "latest_activity_date": "2025-03-03T07:26:58.021Z",
                                  "Name": "Malcolm",
                                  "Id": "975785a867ee3eff1b1e2219a6a15b84",
                                  "PrimaryImageHash": "dtQcC*%M?^?voxV@xvtS-:j?M|og?HkCbbt7OsWCwvt7",
                                  "archived": false
                                },
                                {
                                  "unique_viewers": "2",
                                  "latest_activity_date": "2025-02-25T12:19:06.907Z",
                                  "Name": "Zero Day",
                                  "Id": "3d6f8ded70a1c0cddeae95cd0f2e660f",
                                  "PrimaryImageHash": "dFEVc}~CE19]Hq-UIo0K^%V?R-tR-:aeR+xu9Ft7bFIU",
                                  "archived": false
                                }
                              ]
                        """.trimIndent()
                            )
                            .withHeader("Content-Type", "application/json")
                    )
            )

        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-monthly-top-watched-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer.verify(
            1,
            WireMock.postRequestedFor(WireMock.urlMatching("/stats/getMostPopularByType*")).withHeader("x-api-token", WireMock.equalTo("test-token"))
        )
    }
}
