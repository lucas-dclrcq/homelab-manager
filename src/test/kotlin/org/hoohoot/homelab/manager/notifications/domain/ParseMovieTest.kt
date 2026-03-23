package org.hoohoot.homelab.manager.notifications.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.notifications.RadarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.imdbId
import org.hoohoot.homelab.manager.notifications.quality
import org.hoohoot.homelab.manager.notifications.requester
import org.hoohoot.homelab.manager.notifications.title
import org.hoohoot.homelab.manager.notifications.year
import org.junit.jupiter.api.Test

class ParseMovieTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Test
    fun `from should parse movie payload correctly when all fields are present`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movie": {
                    "title": "Inception",
                    "year": 2010,
                    "imdbId": "tt1375666",
                    "tags": ["Action", "Sci-Fi", "1 - john_doe"]
                },
                "movieFile": {
                    "quality": "1080p"
                }
            }
            """
        )

        assertThat(payload.title()).isEqualTo("Inception")
        assertThat(payload.year()).isEqualTo("2010")
        assertThat(payload.imdbId()).isEqualTo("tt1375666")
        assertThat(payload.quality()).isEqualTo("1080p")
        assertThat(payload.requester()).isEqualTo("john_doe")
    }

    @Test
    fun `from should handle missing movie object`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movieFile": {
                    "quality": "720p"
                }
            }
            """
        )

        assertThat(payload.title()).isEqualTo("unknown")
        assertThat(payload.year()).isEqualTo("unknown")
        assertThat(payload.imdbId()).isEqualTo("unknown")
        assertThat(payload.quality()).isEqualTo("720p")
        assertThat(payload.requester()).isEqualTo("unknown")
    }

    @Test
    fun `from should handle missing movieFile object`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movie": {
                    "title": "Avatar",
                    "year": 2009,
                    "imdbId": "tt0499549",
                    "tags": ["Adventure", "1 - jane_doe"]
                }
            }
            """
        )

        assertThat(payload.title()).isEqualTo("Avatar")
        assertThat(payload.year()).isEqualTo("2009")
        assertThat(payload.imdbId()).isEqualTo("tt0499549")
        assertThat(payload.quality()).isEqualTo("unknown")
        assertThat(payload.requester()).isEqualTo("jane_doe")
    }

    @Test
    fun `from should handle missing tags in movie object`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movie": {
                    "title": "Titanic",
                    "year": 1997,
                    "imdbId": "tt0120338"
                },
                "movieFile": {
                    "quality": "4K"
                }
            }
            """
        )

        assertThat(payload.title()).isEqualTo("Titanic")
        assertThat(payload.year()).isEqualTo("1997")
        assertThat(payload.imdbId()).isEqualTo("tt0120338")
        assertThat(payload.quality()).isEqualTo("4K")
        assertThat(payload.requester()).isEqualTo("unknown")
    }

    @Test
    fun `from should handle empty payload`() {
        val payload = mapper.readValue<RadarrWebhookPayload>("{}")

        assertThat(payload.title()).isEqualTo("unknown")
        assertThat(payload.year()).isEqualTo("unknown")
        assertThat(payload.imdbId()).isEqualTo("unknown")
        assertThat(payload.quality()).isEqualTo("unknown")
        assertThat(payload.requester()).isEqualTo("unknown")
    }
}
