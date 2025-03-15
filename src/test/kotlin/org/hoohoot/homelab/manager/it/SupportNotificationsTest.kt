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
internal class SupportNotificationsTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
        wireMockServer
            .stubFor(
                WireMock.put(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                    .willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withStatus(200)
                            .withBody("{\"event_id\": \"toto\"}")
                    )
            )
    }

    @Test
    fun `should send issue created notification`() {
        val notification = """{
  "notification_type": "ISSUE_CREATED",
  "event": "New Video Issue Reported",
  "subject": "A Complete Unknown (2024)",
  "message": "test",
  "image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/llWl3GtNoXosbvYboelmoT459NM.jpg",
  "media": {
    "media_type": "movie",
    "tmdbId": "661539",
    "tvdbId": "",
    "status": "AVAILABLE",
    "status4k": "UNKNOWN"
  },
  "request": null,
  "issue": {
    "issue_id": "24",
    "issue_type": "VIDEO",
    "issue_status": "OPEN",
    "reportedBy_email": "lucas.declercq@mailbox.org",
    "reportedBy_username": "lucasd",
    "reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
    "reportedBy_settings_discordId": "",
    "reportedBy_settings_telegramChatId": ""
  },
  "comment": null,
  "extra": []
}
""".trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
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
                              "body" : "New Video Issue Reported\nSubject : A Complete Unknown (2024)\nMessage : test\nReporter : lucasd",
                              "format" : "org.matrix.custom.html",
                              "formatted_body" : "<h1>New Video Issue Reported</h1><p>Subject : A Complete Unknown (2024)<br>Message : test<br>Reporter : lucasd</p>",
                              "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send issue created notification with additional info if any`() {
        val notification = """{
  "notification_type": "ISSUE_CREATED",
  "event": "New Video Issue Reported",
  "subject": "A Complete Unknown (2024)",
  "message": "test",
  "image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/llWl3GtNoXosbvYboelmoT459NM.jpg",
  "media": {
    "media_type": "movie",
    "tmdbId": "661539",
    "tvdbId": "",
    "status": "AVAILABLE",
    "status4k": "UNKNOWN"
  },
  "request": null,
  "issue": {
    "issue_id": "24",
    "issue_type": "VIDEO",
    "issue_status": "OPEN",
    "reportedBy_email": "lucas.declercq@mailbox.org",
    "reportedBy_username": "lucasd",
    "reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
    "reportedBy_settings_discordId": "",
    "reportedBy_settings_telegramChatId": ""
  },
  "comment": null,
  "extra": [
    {
      "name": "Affected Season",
	  "value": "30"
    },
    {
      "name": "Affected Episode",
      "value": "10"
    }
  ]
}
""".trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
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
                              "body" : "New Video Issue Reported\nSubject : A Complete Unknown (2024)\nMessage : test\nReporter : lucasd\nAdditional infos :\n- Affected Season : 30\n- Affected Episode : 10",
                              "format" : "org.matrix.custom.html",
                              "formatted_body" : "<h1>New Video Issue Reported</h1><p>Subject : A Complete Unknown (2024)<br>Message : test<br>Reporter : lucasd<br>Additional infos :<br>- Affected Season : 30<br>- Affected Episode : 10</p>",
                              "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send issue resolved notification when no issue created was sent before`() {
        val notification = """
            {
            	"notification_type": "ISSUE_RESOLVED",
            	"event": "Subtitle Issue Resolved",
            	"subject": "Bad Moms (2016)",
            	"message": "test",
            	"image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/u9q10ljhkLj0tNCjlVqe3DCjoU4.jpg",
            	"media": {
            		"media_type": "movie",
            		"tmdbId": "376659",
            		"tvdbId": "",
            		"status": "AVAILABLE",
            		"status4k": "UNKNOWN"
            	},
            	"request": null,
            	"issue": {
            		"issue_id": "30",
            		"issue_type": "SUBTITLES",
            		"issue_status": "RESOLVED",
            		"reportedBy_email": "lucas.declercq@mailbox.org",
            		"reportedBy_username": "lucasd",
            		"reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
            		"reportedBy_settings_discordId": "",
            		"reportedBy_settings_telegramChatId": ""
            	},
            	"comment": null,
            	"extra": []
            }
""".trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype": "m.text",
                              "body": "Subtitle Issue Resolved\nSubject : Bad Moms (2016)\nMessage : test\nReporter : lucasd",
                              "format": "org.matrix.custom.html",
                              "formatted_body": "<h1>Subtitle Issue Resolved</h1><p>Subject : Bad Moms (2016)<br>Message : test<br>Reporter : lucasd</p>",
                               "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send issue resolved notification in thread of created issue`() {
        val issueCreated = """
            {
            	"notification_type": "ISSUE_CREATED",
            	"event": "Subtitle Issue Created",
            	"subject": "Bad Moms (2016)",
            	"message": "test",
            	"image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/u9q10ljhkLj0tNCjlVqe3DCjoU4.jpg",
            	"media": {
            		"media_type": "movie",
            		"tmdbId": "376659",
            		"tvdbId": "",
            		"status": "AVAILABLE",
            		"status4k": "UNKNOWN"
            	},
            	"request": null,
            	"issue": {
            		"issue_id": "28",
            		"issue_type": "SUBTITLES",
            		"issue_status": "RESOLVED",
            		"reportedBy_email": "lucas.declercq@mailbox.org",
            		"reportedBy_username": "lucasd",
            		"reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
            		"reportedBy_settings_discordId": "",
            		"reportedBy_settings_telegramChatId": ""
            	},
            	"comment": null,
            	"extra": []
            }
""".trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(issueCreated)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val issueResolved = """
            {
            	"notification_type": "ISSUE_RESOLVED",
            	"event": "Subtitle Issue Resolved",
            	"subject": "Bad Moms (2016)",
            	"message": "test",
            	"image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/u9q10ljhkLj0tNCjlVqe3DCjoU4.jpg",
            	"media": {
            		"media_type": "movie",
            		"tmdbId": "376659",
            		"tvdbId": "",
            		"status": "AVAILABLE",
            		"status4k": "UNKNOWN"
            	},
            	"request": null,
            	"issue": {
            		"issue_id": "28",
            		"issue_type": "SUBTITLES",
            		"issue_status": "RESOLVED",
            		"reportedBy_email": "lucas.declercq@mailbox.org",
            		"reportedBy_username": "lucasd",
            		"reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
            		"reportedBy_settings_discordId": "",
            		"reportedBy_settings_telegramChatId": ""
            	},
            	"comment": null,
            	"extra": []
            }
""".trimIndent()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.resetAll()

        RestAssured.given().contentType(ContentType.JSON).body(issueResolved)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer.serveEvents.requests.isNotEmpty() }

        wireMockServer.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype": "m.text",
                              "body": "Subtitle Issue Resolved\nSubject : Bad Moms (2016)\nMessage : test\nReporter : lucasd",
                              "format": "org.matrix.custom.html",
                              "formatted_body": "<h1>Subtitle Issue Resolved</h1><p>Subject : Bad Moms (2016)<br>Message : test<br>Reporter : lucasd</p>",
                              "m.relates_to": {
                                  "event_id": "toto",
                                  "rel_type": "m.thread"
                              }
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send issue comment notification in thread of created issue`() {
        val issueCreated = """
            {
            	"notification_type": "ISSUE_CREATED",
            	"event": "Subtitle Issue Created",
            	"subject": "Bad Moms (2016)",
            	"message": "test",
            	"image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/u9q10ljhkLj0tNCjlVqe3DCjoU4.jpg",
            	"media": {
            		"media_type": "movie",
            		"tmdbId": "376659",
            		"tvdbId": "",
            		"status": "AVAILABLE",
            		"status4k": "UNKNOWN"
            	},
            	"request": null,
            	"issue": {
            		"issue_id": "28",
            		"issue_type": "SUBTITLES",
            		"issue_status": "RESOLVED",
            		"reportedBy_email": "lucas.declercq@mailbox.org",
            		"reportedBy_username": "lucasd",
            		"reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
            		"reportedBy_settings_discordId": "",
            		"reportedBy_settings_telegramChatId": ""
            	},
            	"comment": null,
            	"extra": []
            }
""".trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(issueCreated)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val issueResolved = """
            {
            	"notification_type": "ISSUE_COMMENT",
            	"event": "New Comment on Audio Issue",
            	"subject": "Bad Moms (2016)",
            	"message": "test",
            	"image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/u9q10ljhkLj0tNCjlVqe3DCjoU4.jpg",
            	"media": {
            		"media_type": "movie",
            		"tmdbId": "376659",
            		"tvdbId": "",
            		"status": "OPEN",
            		"status4k": "UNKNOWN"
            	},
            	"request": null,
            	"issue": {
            		"issue_id": "28",
            		"issue_type": "SUBTITLES",
            		"issue_status": "RESOLVED",
            		"reportedBy_email": "lucas.declercq@mailbox.org",
            		"reportedBy_username": "lucasd",
            		"reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
            		"reportedBy_settings_discordId": "",
            		"reportedBy_settings_telegramChatId": ""
            	},
            	"comment": {
	            	"comment_message": "some comment",
	            	"commentedBy_email": "lucas.declercq@mailbox.org",
	            	"commentedBy_username": "michel",
	            	"commentedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
	            	"commentedBy_settings_discordId": "",
	            	"commentedBy_settings_telegramChatId": ""
	            },
            	"extra": []
            }
""".trimIndent()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.resetAll()

        RestAssured.given().contentType(ContentType.JSON).body(issueResolved)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer.serveEvents.requests.isNotEmpty() }

        wireMockServer.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype": "m.text",
                              "body": "New Comment on Audio Issue\nSubject : Bad Moms (2016)\nComment : some comment\nComment by : michel",
                              "format": "org.matrix.custom.html",
                              "formatted_body": "<h1>New Comment on Audio Issue</h1><p>Subject : Bad Moms (2016)<br>Comment : some comment<br>Comment by : michel</p>",
                              "m.relates_to": {
                                  "event_id": "toto",
                                  "rel_type": "m.thread"
                              }
                            }
                        """.trimIndent()
                    )
                )
        )
    }

    @Test
    fun `should send notification to correct room`() {
        val notification = """{
  "notification_type": "ISSUE_CREATED",
  "event": "New Video Issue Reported",
  "subject": "A Complete Unknown (2024)",
  "message": "test",
  "image": "https://image.tmdb.org/t/p/w600_and_h900_bestv2/llWl3GtNoXosbvYboelmoT459NM.jpg",
  "media": {
    "media_type": "movie",
    "tmdbId": "661539",
    "tvdbId": "",
    "status": "AVAILABLE",
    "status4k": "UNKNOWN"
  },
  "request": null,
  "issue": {
    "issue_id": "24",
    "issue_type": "VIDEO",
    "issue_status": "OPEN",
    "reportedBy_email": "lucas.declercq@mailbox.org",
    "reportedBy_username": "lucasd",
    "reportedBy_avatar": "/avatarproxy/9af1973a41694f5f84ca268d3a7ce8a2",
    "reportedBy_settings_discordId": "",
    "reportedBy_settings_telegramChatId": ""
  },
  "comment": null,
  "extra": []
}
""".trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/jellyseerr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/!support:test-server.tld/send/m.room.message/.*"))
        )
    }
}