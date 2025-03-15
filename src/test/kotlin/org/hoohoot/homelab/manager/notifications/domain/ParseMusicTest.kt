package org.hoohoot.homelab.manager.notifications.domain

import io.vertx.core.json.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.domain.ParseMusic
import org.junit.jupiter.api.Test

class ParseMusicTest {

    @Test
    fun `should parse album title successfully`() {
        val payload = JsonObject(
            """
            {
                "album": {
                    "title": "A Night at the Opera"
                }
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.albumTitle).isEqualTo("A Night at the Opera")
    }

    @Test
    fun `should fall back to default album title when missing`() {
        val payload = JsonObject(
            """
            {
                "album": {}
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.albumTitle).isEqualTo("unknown")
    }

    @Test
    fun `should parse artist name successfully`() {
        val payload = JsonObject(
            """
            {
                "artist": {
                    "name": "Queen"
                }
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.artistName).isEqualTo("Queen")
    }

    @Test
    fun `should fall back to default artist name when missing`() {
        val payload = JsonObject("{}")
        val album = ParseMusic.from(payload)
        assertThat(album.artistName).isEqualTo("unknown")
    }

    @Test
    fun `should parse cover URL successfully`() {
        val payload = JsonObject(
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
        val album = ParseMusic.from(payload)
        assertThat(album.coverUrl).isEqualTo("cover_image_url")
    }

    @Test
    fun `should fall back to default cover URL when missing`() {
        val payload = JsonObject(
            """
            {
                "album": {
                    "images": []
                }
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.coverUrl).isEqualTo("unknown")
    }

    @Test
    fun `should parse genres successfully`() {
        val payload = JsonObject(
            """
            {
                "album": {
                    "genres": ["Rock", "Progressive Rock"]
                }
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.genres).containsExactly("Rock", "Progressive Rock")
    }

    @Test
    fun `should return empty genres list when none provided`() {
        val payload = JsonObject(
            """
            {
                "album": {}
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.genres).isEmpty()
    }

    @Test
    fun `should parse year successfully`() {
        val payload = JsonObject(
            """
            {
                "album": {
                    "releaseDate": "1975-11-21T00:00:00Z"
                }
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.year).isEqualTo("1975")
    }

    @Test
    fun `should fall back to default year when releaseDate is invalid`() {
        val payload = JsonObject(
            """
            {
                "album": {
                    "releaseDate": "invalid_date"
                }
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.year).isEqualTo("unknown")
    }

    @Test
    fun `should fall back to default year when releaseDate is missing`() {
        val payload = JsonObject(
            """
            {
                "album": {}
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.year).isEqualTo("unknown")
    }

    @Test
    fun `should parse download client successfully`() {
        val payload = JsonObject(
            """
            {
                "downloadClient": "torrent_client"
            }
            """.trimIndent()
        )
        val album = ParseMusic.from(payload)
        assertThat(album.downloadClient).isEqualTo("torrent_client")
    }

    @Test
    fun `should fall back to default download client when missing`() {
        val payload = JsonObject("{}")
        val album = ParseMusic.from(payload)
        assertThat(album.downloadClient).isEqualTo("unknown")
    }
}