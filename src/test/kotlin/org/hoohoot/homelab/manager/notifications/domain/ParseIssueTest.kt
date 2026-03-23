package org.hoohoot.homelab.manager.notifications.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.notifications.SeerrWebhookPayload
import org.hoohoot.homelab.manager.notifications.issueId
import org.hoohoot.homelab.manager.notifications.message
import org.hoohoot.homelab.manager.notifications.notificationType
import org.hoohoot.homelab.manager.notifications.reportedByUserName
import org.hoohoot.homelab.manager.notifications.subject
import org.hoohoot.homelab.manager.notifications.title
import org.junit.jupiter.api.Test

class ParseIssueTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Test
    fun `should parse message`() {
        //arrange
        val payload = mapper.readValue<SeerrWebhookPayload>(
            """
    {
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
        )

        // ACT
        val message = payload.message()


        // ASSERT
        assertThat(message).isEqualTo("test")
    }

    @Test
    fun `should parse notification type`() {
        // ARRANGE
        val payload = mapper.readValue<SeerrWebhookPayload>(
            """
    {
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
        )

        // ACT
        val message = payload.notificationType()

        // ASSERT
        assertThat(message).isEqualTo("ISSUE_CREATED")
    }

    @Test
    fun `should parse issue id`() {
        // ARRANGE
        val payload = mapper.readValue<SeerrWebhookPayload>(
            """
    {
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
        )

        // ACT
        val message = payload.issueId()

        // ASSERT
        assertThat(message).isEqualTo("24")
    }

    @Test
    fun `should parse subject`() {
        //arrange
        val payload = mapper.readValue<SeerrWebhookPayload>(
            """
    {
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
        )

        // act
        val message = payload.subject()

        // assert
        assertThat(message).isEqualTo("A Complete Unknown (2024)")
    }

    @Test
    fun `should parse the reporter`() {
        //arrange
        val payload = mapper.readValue<SeerrWebhookPayload>(
            """
    {
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
        )

        // act
        val message = payload.reportedByUserName()

        // assert
        assertThat(message).isEqualTo("lucasd")
    }

    @Test
    fun `should parse the title`() {
        //arrange
        val payload = mapper.readValue<SeerrWebhookPayload>(
            """
    {
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
        )

        // ACT
        val message = payload.title()

        // ASSERT
        assertThat(message).isEqualTo("New Video Issue Reported")
    }

}
