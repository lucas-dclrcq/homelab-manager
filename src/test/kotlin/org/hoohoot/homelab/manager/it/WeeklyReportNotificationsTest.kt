package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.*
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class WeeklyReportNotificationsTest {
    @InjectSynapse
    private val synapseClient: SynapseClient? = null

    @InjectWireMock
    private val wireMock: WireMockServer? = null

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var mediaRoomId: String

    @BeforeEach
    fun setUp() {
        mediaRoomId = synapseClient!!.createRoom("media-${System.nanoTime()}")
        roomProvider.media = mediaRoomId
        wireMock!!.resetAll()
    }

    @Test
    fun `should send weekly report with all sections`() {
        stubRadarrCalendar("""[
            {"title": "Dune: Part Two", "year": 2024, "digitalRelease": "2025-03-17"},
            {"title": "Oppenheimer", "year": 2024, "physicalRelease": "2025-03-19"}
        ]""")

        stubSonarrCalendar("""[
            {
                "seasonNumber": 3, "episodeNumber": 1, "title": "Tomorrow",
                "airDate": "2025-03-17",
                "series": {"title": "The Bear"}
            }
        ]""")

        stubJellystatMostPopular("Movie", """[
            {"unique_viewers": "5", "Name": "Dune: Part Two"},
            {"unique_viewers": "3", "Name": "Oppenheimer"},
            {"unique_viewers": "2", "Name": "Poor Things"}
        ]""")

        stubJellystatMostPopular("Series", """[
            {"unique_viewers": "8", "Name": "The Bear"},
            {"unique_viewers": "6", "Name": "Severance"}
        ]""")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient!!.getLastMessage(mediaRoomId)
        val body = lastMessage.get("body").asText()

        assertThat(body).contains("📰 Weekly Recap")
        assertThat(body).contains("Monday 17")
        assertThat(body).contains("• Dune: Part Two (2024)")
        assertThat(body).contains("• The Bear S03E01 \"Tomorrow\"")
        assertThat(body).contains("Wednesday 19")
        assertThat(body).contains("• Oppenheimer (2024)")
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

        stubJellystatMostPopular("Movie", "[]")
        stubJellystatMostPopular("Series", "[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseClient!!.getLastMessage(mediaRoomId).get("body").asText()
        val tuesdayIndex = body.indexOf("Tuesday 18")
        val movieIndex = body.indexOf("• Some Movie (2024)")
        val episodeIndex = body.indexOf("• Test Show S01E05 \"Test\"")

        assertThat(tuesdayIndex).isGreaterThan(-1)
        assertThat(movieIndex).isGreaterThan(tuesdayIndex)
        assertThat(episodeIndex).isGreaterThan(tuesdayIndex)
    }

    @Test
    fun `should hide releases section when empty`() {
        stubRadarrCalendar("[]")
        stubSonarrCalendar("[]")

        stubJellystatMostPopular("Movie", """[{"unique_viewers": "3", "Name": "Movie"}]""")
        stubJellystatMostPopular("Series", "[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).doesNotContain("Monday")
        assertThat(body).doesNotContain("Tuesday")
        assertThat(body).contains("🏆 Top 3 Movies This Week")
    }

    @Test
    fun `should hide stats section when no top movies or series`() {
        stubRadarrCalendar("[]")
        stubSonarrCalendar("[]")
        stubJellystatMostPopular("Movie", "[]")
        stubJellystatMostPopular("Series", "[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("📰 Weekly Recap")
        assertThat(body).doesNotContain("🏆")
        assertThat(body).doesNotContain("━━━━━━━━━━━━━━━━━━━━")
    }

    @Test
    fun `should limit top lists to 3 entries`() {
        stubRadarrCalendar("[]")
        stubSonarrCalendar("[]")

        stubJellystatMostPopular("Movie", """[
            {"unique_viewers": "10", "Name": "A"},
            {"unique_viewers": "8", "Name": "B"},
            {"unique_viewers": "5", "Name": "C"},
            {"unique_viewers": "2", "Name": "D"}
        ]""")
        stubJellystatMostPopular("Series", "[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseClient!!.getLastMessage(mediaRoomId).get("body").asText()
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
        stubJellystatMostPopular("Movie", "[]")
        stubJellystatMostPopular("Series", "[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("Thursday 20")
        assertThat(body).contains("• Cinema Movie (2024)")
    }

    @Test
    fun `should show TBD group when no date available`() {
        stubRadarrCalendar("""[
            {"title": "No Date Movie", "year": 2024}
        ]""")
        stubSonarrCalendar("[]")
        stubJellystatMostPopular("Movie", "[]")
        stubJellystatMostPopular("Series", "[]")

        RestAssured.given().contentType(ContentType.JSON)
            .header("X-Api-Key", "secureapikey")
            .`when`().post("/api/notifications/send-weekly-report")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseClient!!.getLastMessage(mediaRoomId).get("body").asText()
        assertThat(body).contains("TBD")
        assertThat(body).contains("• No Date Movie (2024)")
    }

    private fun stubRadarrCalendar(responseBody: String) {
        wireMock!!.stubFor(
            get(urlPathEqualTo("/api/v3/calendar"))
                .withHeader("X-Api-Key", equalTo("API_KEY"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody))
        )
    }

    private fun stubSonarrCalendar(responseBody: String) {
        wireMock!!.stubFor(
            get(urlPathEqualTo("/api/v3/calendar"))
                .withQueryParam("includeSeries", equalTo("true"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody))
        )
    }

    private fun stubJellystatMostPopular(type: String, responseBody: String) {
        wireMock!!.stubFor(
            post(urlPathEqualTo("/stats/getMostPopularByType"))
                .withRequestBody(matchingJsonPath("$.type", equalTo(type)))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody))
        )
    }
}
