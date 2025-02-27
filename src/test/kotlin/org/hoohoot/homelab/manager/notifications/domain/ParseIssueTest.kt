package org.hoohoot.homelab.manager.notifications.domain

import io.vertx.core.json.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.notifications.domain.ParseIssue
import org.junit.jupiter.api.Test

class ParseIssueTest {
    @Test
    fun `should parse message`() {
        //arrange
        val payload = JsonObject(
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
        val message = ParseIssue.from(payload).message


        // ASSERT
        assertThat(message).isEqualTo("test")
    }

    @Test
    fun `should parse notification type`() {
        // ARRANGE
        val payload = JsonObject(
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
        val message = ParseIssue.from(payload).notificationType

        // ASSERT
        assertThat(message).isEqualTo("ISSUE_CREATED")
    }

    @Test
    fun `should parse issue id`() {
        // ARRANGE
        val payload = JsonObject(
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
        val message = ParseIssue.from(payload).issueId

        // ASSERT
        assertThat(message).isEqualTo("24")
    }

    @Test
    fun `should parse subject`() {
        //arrange
        val payload = JsonObject(
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

        val issue = ParseIssue.from(payload)

        // act

        val message = issue.subject

        // assert
        assertThat(message).isEqualTo("A Complete Unknown (2024)")
    }

    @Test
    fun `should parse the reporter`() {
        //arrange
        val payload = JsonObject(
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

        val issue = ParseIssue.from(payload)

        // act

        val message = issue.reportedByUserName

        // assert
        assertThat(message).isEqualTo("lucasd")
    }

    @Test
    fun `should parse the title`() {
        //arrange
        val payload = JsonObject(
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
        val message = ParseIssue.from(payload).title

        // ASSERT
        assertThat(message).isEqualTo("New Video Issue Reported")
    }

}