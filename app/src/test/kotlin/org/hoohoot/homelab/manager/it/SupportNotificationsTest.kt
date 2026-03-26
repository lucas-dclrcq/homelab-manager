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
import org.hoohoot.homelab.manager.notifications.resource.SeerrResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(SeerrResource::class)
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class SupportNotificationsTest {
    @InjectSynapse
    private val synapseTestClient: SynapseTestClient? = null

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var supportRoomId: String

    @BeforeEach
    fun setUp() {
        supportRoomId = synapseTestClient!!.createRoom("support-${System.nanoTime()}")
        roomProvider.support = supportRoomId
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
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient!!.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "🐛 New Video Issue Reported\n📌 Subject : A Complete Unknown (2024)\n💬 Message : test\n👤 Reporter : lucasd"
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>🐛 New Video Issue Reported</h1><p>📌 Subject : A Complete Unknown (2024)<br>💬 Message : test<br>👤 Reporter : lucasd</p>"
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
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient!!.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "🐛 New Video Issue Reported\n📌 Subject : A Complete Unknown (2024)\n💬 Message : test\n👤 Reporter : lucasd\nℹ️ Additional infos :\n- Affected Season : 30\n- Affected Episode : 10"
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>🐛 New Video Issue Reported</h1><p>📌 Subject : A Complete Unknown (2024)<br>💬 Message : test<br>👤 Reporter : lucasd<br>ℹ️ Additional infos :<br>- Affected Season : 30<br>- Affected Episode : 10</p>"
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
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient!!.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "✅ Subtitle Issue Resolved\n📌 Subject : Bad Moms (2016)\n💬 Message : test\n👤 Reporter : lucasd"
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>✅ Subtitle Issue Resolved</h1><p>📌 Subject : Bad Moms (2016)<br>💬 Message : test<br>👤 Reporter : lucasd</p>"
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
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val createdEventId = synapseTestClient!!.getLastMessageEvent(supportRoomId).get("event_id").asText()

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

        RestAssured.given().contentType(ContentType.JSON).body(issueResolved)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "✅ Subtitle Issue Resolved\n📌 Subject : Bad Moms (2016)\n💬 Message : test\n👤 Reporter : lucasd"
        )
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(createdEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
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
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val createdEventId = synapseTestClient!!.getLastMessageEvent(supportRoomId).get("event_id").asText()

        val issueComment = """
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

        RestAssured.given().contentType(ContentType.JSON).body(issueComment)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "💬 New Comment on Audio Issue\n📌 Subject : Bad Moms (2016)\n💬 Comment : some comment\n👤 Comment by : michel"
        )
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(createdEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
    }

    @Test
    fun `should send issue reopened notification in thread of created issue`() {
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
            		"issue_id": "40",
            		"issue_type": "SUBTITLES",
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

        RestAssured.given().contentType(ContentType.JSON).body(issueCreated)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val createdEventId = synapseTestClient!!.getLastMessageEvent(supportRoomId).get("event_id").asText()

        val issueReopened = """
            {
            	"notification_type": "ISSUE_REOPENED",
            	"event": "Subtitle Issue Reopened",
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
            		"issue_id": "40",
            		"issue_type": "SUBTITLES",
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

        RestAssured.given().contentType(ContentType.JSON).body(issueReopened)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "🔄 Subtitle Issue Reopened\n📌 Subject : Bad Moms (2016)\n💬 Message : test\n👤 Reporter : lucasd"
        )
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(createdEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
    }

    @Test
    fun `should add reaction on issue created message when issue is resolved`() {
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
            		"issue_id": "50",
            		"issue_type": "SUBTITLES",
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

        RestAssured.given().contentType(ContentType.JSON).body(issueCreated)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val createdEventId = synapseTestClient!!.getLastMessageEvent(supportRoomId).get("event_id").asText()

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
            		"issue_id": "50",
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

        RestAssured.given().contentType(ContentType.JSON).body(issueResolved)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastReaction = synapseTestClient.getLastReaction(supportRoomId)
        val reactionContent = lastReaction.get("content").get("m.relates_to")
        assertThat(reactionContent.get("event_id").asText()).isEqualTo(createdEventId)
        assertThat(reactionContent.get("key").asText()).isEqualTo("✅")
        assertThat(reactionContent.get("rel_type").asText()).isEqualTo("m.annotation")
    }
}
