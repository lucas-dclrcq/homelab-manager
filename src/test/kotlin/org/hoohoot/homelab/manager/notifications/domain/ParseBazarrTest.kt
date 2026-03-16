package org.hoohoot.homelab.manager.notifications.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hoohoot.homelab.manager.notifications.BazarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.SubtitleDownload
import org.hoohoot.homelab.manager.notifications.mediaKey
import org.junit.jupiter.api.Test

class ParseBazarrTest {

    @Test
    fun `should parse movie subtitle body`() {
        val payload = BazarrWebhookPayload(
            body = "Inception (2010) : French subtitles downloaded from opensubtitles with a score of 95%."
        )

        val subtitle = SubtitleDownload.from(payload)

        assertThat(subtitle.mediaTitle).isEqualTo("Inception")
        assertThat(subtitle.year).isEqualTo("2010")
        assertThat(subtitle.language).isEqualTo("French")
        assertThat(subtitle.action).isEqualTo("downloaded")
        assertThat(subtitle.provider).isEqualTo("opensubtitles")
        assertThat(subtitle.score).isEqualTo("95")
        assertThat(subtitle.episodeInfo).isNull()
    }

    @Test
    fun `should parse series subtitle body`() {
        val payload = BazarrWebhookPayload(
            body = "Breaking Bad (2008) - S01E02 - Cat's in the Bag... : English subtitles downloaded from opensubtitles with a score of 90%."
        )

        val subtitle = SubtitleDownload.from(payload)

        assertThat(subtitle.mediaTitle).isEqualTo("Breaking Bad")
        assertThat(subtitle.year).isEqualTo("2008")
        assertThat(subtitle.language).isEqualTo("English")
        assertThat(subtitle.action).isEqualTo("downloaded")
        assertThat(subtitle.provider).isEqualTo("opensubtitles")
        assertThat(subtitle.score).isEqualTo("90")
        assertThat(subtitle.episodeInfo).isEqualTo("S01E02 - Cat's in the Bag...")
    }

    @Test
    fun `should parse upgraded subtitle action`() {
        val payload = BazarrWebhookPayload(
            body = "Inception (2010) : French subtitles upgraded from addic7ed with a score of 98%."
        )

        val subtitle = SubtitleDownload.from(payload)

        assertThat(subtitle.action).isEqualTo("upgraded")
        assertThat(subtitle.provider).isEqualTo("addic7ed")
        assertThat(subtitle.score).isEqualTo("98")
    }

    @Test
    fun `should parse manually downloaded subtitle action`() {
        val payload = BazarrWebhookPayload(
            body = "Inception (2010) : French subtitles manually downloaded from subscene with a score of 85%."
        )

        val subtitle = SubtitleDownload.from(payload)

        assertThat(subtitle.action).isEqualTo("manually downloaded")
        assertThat(subtitle.provider).isEqualTo("subscene")
    }

    @Test
    fun `should parse HI subtitle language`() {
        val payload = BazarrWebhookPayload(
            body = "Inception (2010) : English (HI) subtitles downloaded from opensubtitles with a score of 92%."
        )

        val subtitle = SubtitleDownload.from(payload)

        assertThat(subtitle.language).isEqualTo("English (HI)")
    }

    @Test
    fun `should parse forced subtitle language`() {
        val payload = BazarrWebhookPayload(
            body = "Inception (2010) : French (forced) subtitles downloaded from opensubtitles with a score of 80%."
        )

        val subtitle = SubtitleDownload.from(payload)

        assertThat(subtitle.language).isEqualTo("French (forced)")
    }

    @Test
    fun `should throw on null body`() {
        val payload = BazarrWebhookPayload(body = null)

        assertThatThrownBy { SubtitleDownload.from(payload) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("body is missing")
    }

    @Test
    fun `should throw on unparseable body`() {
        val payload = BazarrWebhookPayload(body = "some random text")

        assertThatThrownBy { SubtitleDownload.from(payload) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Unable to parse")
    }

    @Test
    fun `mediaKey should be consistent between radarr and bazarr`() {
        val radarrKey = mediaKey("Inception", "2010")
        val bazarrPayload = BazarrWebhookPayload(
            body = "Inception (2010) : French subtitles downloaded from opensubtitles with a score of 95%."
        )
        val subtitle = SubtitleDownload.from(bazarrPayload)
        val bazarrKey = mediaKey(subtitle.mediaTitle, subtitle.year)

        assertThat(radarrKey).isEqualTo(bazarrKey)
        assertThat(radarrKey).isEqualTo("inception:2010")
    }

    @Test
    fun `mediaKey should normalize case and whitespace`() {
        val key1 = mediaKey("  Inception  ", " 2010 ")
        val key2 = mediaKey("inception", "2010")

        assertThat(key1).isEqualTo(key2)
        assertThat(key1).isEqualTo("inception:2010")
    }
}
