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
class SendWhatsNextWeeklyReportTest {
    @Inject
    @field:Default
    lateinit var timeService: TimeService

    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        val sonarrCalendar = """
            [
              {
                "seriesId": 319,
                "tvdbId": 10881936,
                "episodeFileId": 0,
                "seasonNumber": 2,
                "episodeNumber": 8,
                "title": "The Innocents",
                "airDate": "2025-03-02",
                "airDateUtc": "2025-03-03T02:00:00Z",
                "runtime": 0,
                "hasFile": false,
                "monitored": true,
                "absoluteEpisodeNumber": 16,
                "unverifiedSceneNumbering": false,
                "series": {
                  "title": "Anne Rice’s Mayfair Witches",
                  "sortTitle": "anne rices mayfair witches",
                  "status": "continuing",
                  "ended": false,
                  "overview": "Rowan Fielding, an intuitive young neurosurgeon, discovers that she is the unlikely heir to a family of witches; as she grapples with her newfound powers, she must contend with a sinister presence that has haunted her family for generations.",
                  "network": "AMC",
                  "airTime": "21:00",
                  "images": [
                    {
                      "coverType": "banner",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/413074/banners/6390f289e1e39.jpg"
                    },
                    {
                      "coverType": "poster",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/413074/posters/67759a0d7c8a6.jpg"
                    },
                    {
                      "coverType": "fanart",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/413074/backgrounds/63936908799ef.jpg"
                    },
                    {
                      "coverType": "clearlogo",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/413074/clearlogo/635d6763e6f8e.png"
                    }
                  ],
                  "originalLanguage": {
                    "id": 1,
                    "name": "English"
                  },
                  "seasons": [
                    {
                      "seasonNumber": 0,
                      "monitored": false
                    },
                    {
                      "seasonNumber": 1,
                      "monitored": true
                    },
                    {
                      "seasonNumber": 2,
                      "monitored": true
                    }
                  ],
                  "year": 2023,
                  "path": "/media/TV/Mayfair Witches (2023) [tvdbid-413074]",
                  "qualityProfileId": 40,
                  "seasonFolder": true,
                  "monitored": true,
                  "monitorNewItems": "all",
                  "useSceneNumbering": false,
                  "runtime": 45,
                  "tvdbId": 413074,
                  "tvRageId": 0,
                  "tvMazeId": 59249,
                  "tmdbId": 207863,
                  "firstAired": "2023-01-08T00:00:00Z",
                  "lastAired": "2025-03-02T00:00:00Z",
                  "seriesType": "standard",
                  "cleanTitle": "annericesmayfairwitches",
                  "imdbId": "tt15428778",
                  "titleSlug": "anne-rices-mayfair-witches",
                  "genres": [
                    "Drama",
                    "Fantasy",
                    "Horror"
                  ],
                  "tags": [
                    39,
                    1
                  ],
                  "added": "2025-01-01T21:39:51Z",
                  "ratings": {
                    "votes": 15859,
                    "value": 6.2
                  },
                  "languageProfileId": 1,
                  "id": 319
                },
                "id": 16818
              },
              {
                "seriesId": 102,
                "tvdbId": 10855676,
                "episodeFileId": 0,
                "seasonNumber": 3,
                "episodeNumber": 3,
                "title": "The Meaning of Dreams",
                "airDate": "2025-03-02",
                "airDateUtc": "2025-03-03T02:00:00Z",
                "runtime": 0,
                "overview": "Jaclyn pushes the idea of a vacation fling on Laurie. Chelsea finds herself in a perilous situation. Gaitok worries about a reprimand.",
                "hasFile": false,
                "monitored": true,
                "unverifiedSceneNumbering": false,
                "series": {
                  "title": "The White Lotus",
                  "sortTitle": "white lotus",
                  "status": "continuing",
                  "ended": false,
                  "overview": "Taking place over one tumultuous week, this social satire follows the exploits of various guests and employees at an exclusive resort.",
                  "network": "HBO",
                  "airTime": "21:00",
                  "images": [
                    {
                      "coverType": "banner",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/390430/banners/6103209879e5a.jpg"
                    },
                    {
                      "coverType": "poster",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/390430/posters/6797ba78b376a.jpg"
                    },
                    {
                      "coverType": "fanart",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/390430/backgrounds/60db33aea89eb.jpg"
                    },
                    {
                      "coverType": "clearlogo",
                      "remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/390430/clearlogo/611d49a409e83.png"
                    }
                  ],
                  "originalLanguage": {
                    "id": 1,
                    "name": "English"
                  },
                  "seasons": [
                    {
                      "seasonNumber": 0,
                      "monitored": false
                    },
                    {
                      "seasonNumber": 1,
                      "monitored": true
                    },
                    {
                      "seasonNumber": 2,
                      "monitored": true
                    },
                    {
                      "seasonNumber": 3,
                      "monitored": true
                    }
                  ],
                  "year": 2021,
                  "path": "/media/TV/The White Lotus",
                  "qualityProfileId": 40,
                  "seasonFolder": true,
                  "monitored": true,
                  "monitorNewItems": "all",
                  "useSceneNumbering": false,
                  "runtime": 59,
                  "tvdbId": 390430,
                  "tvRageId": 0,
                  "tvMazeId": 51394,
                  "tmdbId": 111803,
                  "firstAired": "2021-07-11T00:00:00Z",
                  "lastAired": "2025-04-06T00:00:00Z",
                  "seriesType": "standard",
                  "cleanTitle": "thewhitelotus",
                  "imdbId": "tt13406094",
                  "titleSlug": "the-white-lotus",
                  "certification": "TV-MA",
                  "genres": [
                    "Comedy",
                    "Drama"
                  ],
                  "tags": [
                    39,
                    1
                  ],
                  "added": "2024-09-07T14:08:55Z",
                  "ratings": {
                    "votes": 239608,
                    "value": 8.0
                  },
                  "languageProfileId": 1,
                  "id": 102
                },
                "id": 15366
              }
            ]
                """.trimIndent()
        wireMockServer!!.resetAll()
        wireMockServer
            .stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/v3/calendar"))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody(sonarrCalendar)
                        .withHeader("Content-Type", "application/json")
                    )
            )
        wireMockServer
            .stubFor(
                WireMock.put(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                    .willReturn(WireMock.aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"event_id\": \"toto\"}"))
            )
    }

    @Test
    fun `should send whats next report`() {
        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-whats-next-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer!!.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype" : "m.text",
                              "body" : "What's next report\n\nSeries :\n2025-03-02 : Anne Rice’s Mayfair Witches - S02E08 - The Innocents\n2025-03-02 : The White Lotus - S03E03 - The Meaning of Dreams",
                              "format" : "org.matrix.custom.html",
                              "formatted_body" : "<h1>What's next report</h1><p><br>Series :<br>2025-03-02 : Anne Rice’s Mayfair Witches - S02E08 - The Innocents<br>2025-03-02 : The White Lotus - S03E03 - The Meaning of Dreams</p>",
                              "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send whats next report to correct room`() {
        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-whats-next-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer!!.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/!media:test-server.tld/send/m.room.message/.*"))
        )
    }

    @Test
    fun `should send whats next report with series for current week`() {
        this.timeService.setFixedClock(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse("2023-10-18T12:34:56Z")), ZoneId.systemDefault())

        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-whats-next-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer!!.verify(
            1, WireMock.getRequestedFor(WireMock.urlPathMatching("/api/v3/calendar"))
                .withQueryParam("start", WireMock.equalTo("2023-10-16T00:00:00Z"))
                .withQueryParam("end", WireMock.equalTo("2023-10-22T23:59:00Z"))
        )
    }

    @Test
    fun `should ask sonarr api to include series details`() {
        RestAssured.given().contentType(ContentType.JSON)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/send-whats-next-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        wireMockServer!!.verify(
            1, WireMock.getRequestedFor(WireMock.urlPathMatching("/api/v3/calendar"))
                .withQueryParam("includeSeries", WireMock.equalTo("true"))
        )
    }
}
