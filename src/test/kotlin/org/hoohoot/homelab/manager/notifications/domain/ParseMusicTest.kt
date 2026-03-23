package org.hoohoot.homelab.manager.notifications.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.notifications.LidarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.albumTitle
import org.hoohoot.homelab.manager.notifications.artistName
import org.hoohoot.homelab.manager.notifications.coverUrl
import org.hoohoot.homelab.manager.notifications.source
import org.hoohoot.homelab.manager.notifications.genres
import org.hoohoot.homelab.manager.notifications.year
import org.junit.jupiter.api.Test

class ParseMusicTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Test
    fun `should parse album title successfully`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {
                    "title": "A Night at the Opera"
                }
            }
            """.trimIndent()
        )
        assertThat(payload.albumTitle()).isEqualTo("A Night at the Opera")
    }

    @Test
    fun `should fall back to default album title when missing`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {}
            }
            """.trimIndent()
        )
        assertThat(payload.albumTitle()).isEqualTo("unknown")
    }

    @Test
    fun `should parse artist name successfully`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "artist": {
                    "name": "Queen"
                }
            }
            """.trimIndent()
        )
        assertThat(payload.artistName()).isEqualTo("Queen")
    }

    @Test
    fun `should fall back to default artist name when missing`() {
        val payload = mapper.readValue<LidarrWebhookPayload>("{}")
        assertThat(payload.artistName()).isEqualTo("unknown")
    }

    @Test
    fun `should parse cover URL successfully`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {
                    "images": [
                        {"coverType": "thumb", "remoteUrl": "thumb_image_url"},
                        {"coverType": "cover", "remoteUrl": "cover_image_url"}
                    ]
                }
            }
            """.trimIndent()
        )
        assertThat(payload.coverUrl()).isEqualTo("cover_image_url")
    }

    @Test
    fun `should fall back to default cover URL when missing`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {
                    "images": []
                }
            }
            """.trimIndent()
        )
        assertThat(payload.coverUrl()).isEqualTo("unknown")
    }

    @Test
    fun `should parse genres successfully`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {
                    "genres": ["Rock", "Progressive Rock"]
                }
            }
            """.trimIndent()
        )
        assertThat(payload.genres()).containsExactly("Rock", "Progressive Rock")
    }

    @Test
    fun `should return empty genres list when none provided`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {}
            }
            """.trimIndent()
        )
        assertThat(payload.genres()).isEmpty()
    }

    @Test
    fun `should parse year successfully`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {
                    "releaseDate": "1975-11-21T00:00:00Z"
                }
            }
            """.trimIndent()
        )
        assertThat(payload.year()).isEqualTo("1975")
    }

    @Test
    fun `should fall back to default year when releaseDate is invalid`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {
                    "releaseDate": "invalid_date"
                }
            }
            """.trimIndent()
        )
        assertThat(payload.year()).isEqualTo("unknown")
    }

    @Test
    fun `should fall back to default year when releaseDate is missing`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "album": {}
            }
            """.trimIndent()
        )
        assertThat(payload.year()).isEqualTo("unknown")
    }

    @Test
    fun `should parse download client successfully`() {
        val payload = mapper.readValue<LidarrWebhookPayload>(
            """
            {
                "downloadClient": "torrent_client"
            }
            """.trimIndent()
        )
        assertThat(payload.source()).isEqualTo("torrent_client")
    }

    @Test
    fun `should fall back to default download client when missing`() {
        val payload = mapper.readValue<LidarrWebhookPayload>("{}")
        assertThat(payload.source()).isEqualTo("unknown")
    }
}
