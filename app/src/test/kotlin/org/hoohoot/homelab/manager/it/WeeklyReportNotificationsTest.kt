package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.PlaybackSessionSeed
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.shared.matrix.MatrixRoomProvider
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class WeeklyReportNotificationsTest {
    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var wireMock: WireMock

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var mediaRoomId: String

    @BeforeEach
    fun setUp() {
        mediaRoomId = synapseTestClient.createRoom("media-${System.nanoTime()}")
        roomProvider.media = mediaRoomId
        wireMock.resetMappings()
        // Le rapport agrège playback_session sur 7 jours glissants : purge pour que
        // les sessions seedées par les autres classes de test ne polluent pas les tops
        PlaybackSessionSeed.deleteAll()
    }

    private fun seedMovieViewers(title: String, viewers: Int) {
        repeat(viewers) { viewer ->
            PlaybackSessionSeed.insertSession(
                userName = "$title-viewer-$viewer",
                itemName = title,
                mediaType = MediaType.MOVIE,
            )
        }
    }

    private fun seedSeriesViewers(title: String, viewers: Int) {
        repeat(viewers) { viewer ->
            PlaybackSessionSeed.insertSession(
                userName = "$title-viewer-$viewer",
                itemId = "$title-s01e01",
                itemName = "Episode 1",
                mediaType = MediaType.EPISODE,
                seriesId = title,
                seriesName = title,
                seasonNumber = 1,
                episodeNumber = 1,
            )
        }
    }

    @Test
    fun `should send weekly report with all sections`() {
        stubRadarrCalendar("""[
            {"title": "Dune: Part Two", "year": 2024, "digitalRelease": "2025-03-17T20:00:00Z"},
            {"title": "Oppenheimer", "year": 2024, "physicalRelease": "2025-03-19"}
        ]""")

        stubSonarrCalendar("""[
            {
                "seasonNumber": 3, "episodeNumber": 1, "title": "Tomorrow",
                "airDate": "2025-03-17", "airDateUtc": "2025-03-17T21:00:00Z",
                "series": {"title": "The Bear"}
            }
        ]""")

        stubLidarrCalendar("""[
            {"title": "GNX", "releaseDate": "2025-03-18", "artist": {"artistName": "Kendrick Lamar"}}
        ]""")

        seedMovieViewers("Dune: Part Two", 5)
        seedMovieViewers("Oppenheimer", 3)
        seedMovieViewers("Poor Things", 2)
        seedSeriesViewers("The Bear", 8)
        seedSeriesViewers("Severance", 6)

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(mediaRoomId)
        val body = lastMessage.get("body").asText()

        assertThat(body).contains("📰 Weekly Recap")
        assertThat(body).contains("Monday 17")
        val duneTime = localTime("2025-03-17T20:00:00Z")
        val bearTime = localTime("2025-03-17T21:00:00Z")
        assertThat(body).contains("🎬 Dune: Part Two (2024) — $duneTime")
        assertThat(body).contains("📺 The Bear S03E01 \"Tomorrow\" — $bearTime")
        assertThat(body).contains("Wednesday 19")
        assertThat(body).contains("🎬 Oppenheimer (2024)")
        assertThat(body).contains("Tuesday 18")
        assertThat(body).contains("🎵 Kendrick Lamar — GNX")
        assertThat(body).contains("🏆 Top 3 Movies This Week")
        assertThat(body).contains("🥇 Dune: Part Two — 5 viewers")
        assertThat(body).contains("🥈 Oppenheimer — 3 viewers")
        assertThat(body).contains("🥉 Poor Things — 2 viewers")
        assertThat(body).contains("🏆 Top 3 Series This Week")
        assertThat(body).contains("🥇 The Bear — 8 viewers")
        assertThat(body).contains("🥈 Severance — 6 viewers")

        val formattedBody = lastMessage.get("formatted_body").asText()
        assertThat(formattedBody).contains("<h2>📰 Weekly Recap</h2>")
        assertThat(formattedBody).contains("<hr>")
        assertThat(formattedBody).contains("<b>Monday 17</b>")
        assertThat(formattedBody).contains("<b>Wednesday 19</b>")
        assertThat(formattedBody).contains("<b>🏆 Top 3 Movies This Week</b>")
        assertThat(formattedBody).contains("<b>🏆 Top 3 Series This Week</b>")
    }

    @Test
    fun `should group movies and episodes on same day together`() {
        stubRadarrCalendar("""[
            {"title": "Some Movie", "year": 2024, "digitalRelease": "2025-03-18"}
        ]""")

        stubSonarrCalendar("""[
            {
                "seasonNumber": 1, "episodeNumber": 5, "title": "Test",
                "airDate": "2025-03-18",
                "series": {"title": "Test Show"}
            }
        ]""")

        stubLidarrCalendar("[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        val tuesdayIndex = body.indexOf("Tuesday 18")
        val movieIndex = body.indexOf("🎬 Some Movie (2024)")
        val episodeIndex = body.indexOf("📺 Test Show S01E05 \"Test\"")

        assertThat(tuesdayIndex).isGreaterThan(-1)
        assertThat(movieIndex).isGreaterThan(tuesdayIndex)
        assertThat(episodeIndex).isGreaterThan(tuesdayIndex)
    }

    @Test
    fun `should hide releases section when empty`() {
        stubRadarrCalendar("[]")
        stubSonarrCalendar("[]")
        stubLidarrCalendar("[]")

        seedMovieViewers("Movie", 3)

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).doesNotContain("Monday")
        assertThat(body).doesNotContain("Tuesday")
        assertThat(body).contains("🏆 Top 3 Movies This Week")
    }

    @Test
    fun `should hide stats section when no top movies or series`() {
        stubRadarrCalendar("[]")
        stubSonarrCalendar("[]")
        stubLidarrCalendar("[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("📰 Weekly Recap")
        assertThat(body).doesNotContain("🏆")
        assertThat(body).doesNotContain("━━━━━━━━━━━━━━━━━━━━")
    }

    @Test
    fun `should limit top lists to 3 entries`() {
        stubRadarrCalendar("[]")
        stubSonarrCalendar("[]")
        stubLidarrCalendar("[]")

        seedMovieViewers("A", 10)
        seedMovieViewers("B", 8)
        seedMovieViewers("C", 5)
        seedMovieViewers("D", 2)

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("🥇 A — 10 viewers")
        assertThat(body).contains("🥈 B — 8 viewers")
        assertThat(body).contains("🥉 C — 5 viewers")
        assertThat(body).doesNotContain("D — 2 viewers")
    }

    @Test
    fun `should handle movie with only inCinemas date`() {
        stubRadarrCalendar("""[
            {"title": "Cinema Movie", "year": 2024, "inCinemas": "2025-03-20"}
        ]""")
        stubSonarrCalendar("[]")
        stubLidarrCalendar("[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("Thursday 20")
        assertThat(body).contains("🎬 Cinema Movie (2024)")
    }

    @Test
    fun `should show TBD group when no date available`() {
        stubRadarrCalendar("""[
            {"title": "No Date Movie", "year": 2024}
        ]""")
        stubSonarrCalendar("[]")
        stubLidarrCalendar("[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("TBD")
        assertThat(body).contains("🎬 No Date Movie (2024)")
    }

    private fun stubRadarrCalendar(responseBody: String) {
        wireMock.register(
            get(urlPathEqualTo("/api/v3/calendar"))
                .withHeader("X-Api-Key", equalTo("API_KEY"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody))
        )
    }

    private fun stubSonarrCalendar(responseBody: String) {
        wireMock.register(
            get(urlPathEqualTo("/api/v3/calendar"))
                .withQueryParam("includeSeries", equalTo("true"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody))
        )
    }

    private fun localTime(utcDateTime: String): String {
        val local = Instant.parse(utcDateTime).toLocalDateTime(TimeZone.currentSystemDefault())
        return "%02d:%02d".format(local.hour, local.minute)
    }

    private fun stubLidarrCalendar(responseBody: String) {
        wireMock.register(
            get(urlPathEqualTo("/api/v1/calendar"))
                .withHeader("X-Api-Key", equalTo("API_KEY"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody))
        )
    }
}
