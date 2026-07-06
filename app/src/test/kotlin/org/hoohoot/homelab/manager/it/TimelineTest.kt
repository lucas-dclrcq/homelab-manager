package org.hoohoot.homelab.manager.it

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class TimelineTest {
    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    @BeforeEach
    fun setUp() {
        roomProvider.media = synapseTestClient.createRoom("media-${System.nanoTime()}")
    }

    private fun postWebhook(path: String, payload: String) {
        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .header("X-Api-Key", "secureapikey")
            .`when`().post(path)
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
    }

    private fun getTimeline(page: Int = 0, pageSize: Int = 100): JsonPath =
        RestAssured.given()
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .`when`().get("/api/timeline")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun eventsWithTitle(timeline: JsonPath, title: String): List<Map<String, Any>> =
        timeline.getList<Map<String, Any>>("items").filter { (it["title"] as String).contains(title) }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `movie download webhook creates a timeline event`() {
        val title = "Movie-${System.nanoTime()}"
        postWebhook(
            "/api/notifications/radarr", """
            {
              "movie": { "id": 42, "title": "$title", "year": 2024, "imdbId": "tt0000001", "tags": ["1 - lucasd"] },
              "movieFile": { "quality": "WEBDL-1080p" },
              "eventType": "Download"
            }
        """.trimIndent()
        )

        val events = eventsWithTitle(getTimeline(), title)
        assertThat(events).hasSize(1)
        assertThat(events.first()["eventType"]).isEqualTo("movie_downloaded")
        assertThat(events.first()["title"]).isEqualTo("$title (2024)")
        @Suppress("UNCHECKED_CAST")
        val details = events.first()["details"] as Map<String, String>
        assertThat(details["quality"]).isEqualTo("WEBDL-1080p")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `episode download webhook creates one timeline event per episode`() {
        val title = "Series-${System.nanoTime()}"
        postWebhook(
            "/api/notifications/sonarr", """
            {
              "series": { "id": 43, "title": "$title", "year": 2022, "imdbId": "tt0000002", "tags": ["11 - flo"] },
              "episodes": [
                { "episodeNumber": 1, "seasonNumber": 2, "title": "Premier" },
                { "episodeNumber": 2, "seasonNumber": 2, "title": "Second" }
              ],
              "episodeFile": { "quality": "WEBDL-720p" },
              "release": { "indexer": "NZBFinder (Prowlarr)" },
              "downloadClient": "SABnzbd",
              "eventType": "Download"
            }
        """.trimIndent()
        )

        val events = eventsWithTitle(getTimeline(), title)
        assertThat(events).hasSize(2)
        assertThat(events.map { it["eventType"] }).containsOnly("episode_downloaded")
        val episodeNumbers = events.map { (it["details"] as Map<*, *>)["episodeNumber"] }
        assertThat(episodeNumbers).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `subtitle download webhook creates a timeline event`() {
        val title = "Sub-${System.nanoTime()}"
        postWebhook(
            "/api/notifications/bazarr", """
            {
              "title": "Bazarr",
              "message": "$title (2014) : French subtitles downloaded from opensubtitles with a score of 95%.",
              "type": "info"
            }
        """.trimIndent()
        )

        val events = eventsWithTitle(getTimeline(), title)
        assertThat(events).hasSize(1)
        assertThat(events.first()["eventType"]).isEqualTo("subtitles_downloaded")
        @Suppress("UNCHECKED_CAST")
        val details = events.first()["details"] as Map<String, String>
        assertThat(details["language"]).isEqualTo("French")
        assertThat(details["provider"]).isEqualTo("opensubtitles")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `grab events do not create timeline events`() {
        val title = "Grabbed-${System.nanoTime()}"
        postWebhook(
            "/api/notifications/radarr", """
            { "movie": { "id": 1, "title": "$title", "year": 2024 }, "eventType": "Grab" }
        """.trimIndent()
        )

        assertThat(eventsWithTitle(getTimeline(), title)).isEmpty()
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `timeline is paginated and sorted by most recent first`() {
        val title = "Paged-${System.nanoTime()}"
        for (index in 1..3) {
            postWebhook(
                "/api/notifications/radarr", """
                {
                  "movie": { "id": ${100 + index}, "title": "$title-$index", "year": 2024 },
                  "movieFile": { "quality": "WEBDL-1080p" },
                  "eventType": "Download"
                }
            """.trimIndent()
            )
        }

        val firstPage = getTimeline(page = 0, pageSize = 1)
        assertThat(firstPage.getList<Any>("items")).hasSize(1)
        assertThat(firstPage.getInt("pageSize")).isEqualTo(1)
        val totalCount = firstPage.getLong("totalCount")
        assertThat(totalCount).isGreaterThanOrEqualTo(3)
        assertThat(firstPage.getInt("totalPages")).isEqualTo(totalCount.toInt())

        // The most recently posted download must be the first item of the first page
        assertThat(firstPage.getString("items[0].title")).isEqualTo("$title-3 (2024)")

        val secondPage = getTimeline(page = 1, pageSize = 1)
        assertThat(secondPage.getString("items[0].title")).isEqualTo("$title-2 (2024)")
    }

    @Test
    fun `anonymous user cannot read the timeline`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get("/api/timeline")
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
