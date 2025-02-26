package org.hoohoot.homelab.manager

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.awaitility.Awaitility
import org.hoohoot.homelab.manager.config.InjectWireMock
import org.hoohoot.homelab.manager.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.resource.NotificationsResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource::class)
@QuarkusTestResource(WiremockTestResource::class)
internal class JellySeerrNotificationsTest {
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
    fun shouldSendIssueCreatedNotification() {
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
            .`when`().post("jellyseerr")
            .then().statusCode(200)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                        {
                          "msgtype": "m.text",
                          "body": "<h1>New Video Issue Reported</h1><p>- Subject : A Complete Unknown (2024)<br>- Message : test<br>- Reporter : lucasd</p>",
                          "format": "org.matrix.custom.html",
                          "formatted_body": "<h1>New Video Issue Reported</h1><p>- Subject : A Complete Unknown (2024)<br>- Message : test<br>- Reporter : lucasd</p>"
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
            .`when`().post("jellyseerr")
            .then().statusCode(200)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/!jellyseerr:test-server.tld/send/m.room.message/.*"))
        )
    }
}