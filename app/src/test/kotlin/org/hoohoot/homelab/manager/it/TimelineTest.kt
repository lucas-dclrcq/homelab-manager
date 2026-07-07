package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.vertx.VertxContextSupport
import io.restassured.RestAssured
import io.restassured.path.json.JsonPath
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.portal.persistence.MediaDownloadEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@QuarkusTest
internal class TimelineTest {

    @Inject
    lateinit var wireMock: WireMock

    private val yesterday: Instant = Instant.now().minus(1, ChronoUnit.DAYS)
    private val twoDaysAgo: Instant = Instant.now().minus(2, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction { MediaDownloadEntity.deleteAll() }
        }

        wireMock.resetMappings()
        wireMock.resetRequests()
        // Radarr and Sonarr share the same WireMock: disambiguate /history/since by query param
        wireMock.register(
            get(urlPathEqualTo("/api/v3/history/since"))
                .withQueryParam("includeMovie", equalTo("true"))
                .willReturn(
                    okJson(
                        """[
                            {
                              "id": 1001, "movieId": 5, "eventType": "downloadFolderImported", "date": "$yesterday",
                              "quality": {"quality": {"name": "WEBDL-1080p"}},
                              "movie": {"id": 5, "title": "Dune", "year": 2024}
                            },
                            {
                              "id": 1002, "movieId": 6, "eventType": "grabbed", "date": "$yesterday",
                              "movie": {"id": 6, "title": "Grabbed Movie", "year": 2024}
                            }
                        ]"""
                    )
                )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/history/since"))
                .withQueryParam("includeSeries", equalTo("true"))
                .willReturn(
                    okJson(
                        """[
                            {
                              "id": 2001, "seriesId": 10, "episodeId": 100, "eventType": "downloadFolderImported", "date": "$yesterday",
                              "quality": {"quality": {"name": "WEBDL-720p"}},
                              "series": {"id": 10, "title": "The Bear"},
                              "episode": {"id": 100, "seasonNumber": 2, "episodeNumber": 1, "title": "Premier"}
                            },
                            {
                              "id": 2002, "seriesId": 10, "episodeId": 101, "eventType": "downloadFolderImported", "date": "$twoDaysAgo",
                              "quality": {"quality": {"name": "WEBDL-720p"}},
                              "series": {"id": 10, "title": "The Bear"},
                              "episode": {"id": 101, "seasonNumber": 2, "episodeNumber": 2, "title": "Second"}
                            },
                            {
                              "id": 2003, "seriesId": 10, "episodeId": 100, "eventType": "episodeFileDeleted", "date": "$yesterday",
                              "series": {"id": 10, "title": "The Bear"}
                            }
                        ]"""
                    )
                )
        )
        // Lidarr est sur /api/v1 : pas de collision avec les stubs /api/v3 de Radarr/Sonarr
        wireMock.register(
            get(urlPathEqualTo("/api/v1/history/since")).willReturn(
                okJson(
                    """[
                        {
                          "id": 3001, "albumId": 20, "artistId": 30, "eventType": "downloadImported", "date": "$twoDaysAgo",
                          "quality": {"quality": {"name": "FLAC"}},
                          "album": {"id": 20, "title": "Random Access Memories"},
                          "artist": {"id": 30, "artistName": "Daft Punk"}
                        },
                        {
                          "id": 3002, "albumId": 20, "artistId": 30, "eventType": "trackFileImported", "date": "$twoDaysAgo",
                          "album": {"id": 20, "title": "Random Access Memories"},
                          "artist": {"id": 30, "artistName": "Daft Punk"}
                        }
                    ]"""
                )
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/episodes/history")).willReturn(
                okJson(
                    """{
                        "data": [
                          {
                            "seriesTitle": "The Bear", "episodeTitle": "Premier", "episode_number": "2x01",
                            "language": {"name": "French", "code2": "fr"}, "provider": "opensubtitles",
                            "action": 1, "raw_timestamp": "$yesterday", "sonarrEpisodeId": 100
                          },
                          {
                            "seriesTitle": "The Bear", "episodeTitle": "Premier", "episode_number": "2x01",
                            "language": {"name": "French", "code2": "fr"}, "provider": "opensubtitles",
                            "action": 0, "raw_timestamp": "$yesterday", "sonarrEpisodeId": 100
                          }
                        ]
                    }"""
                )
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/movies/history")).willReturn(
                okJson(
                    """{
                        "data": [
                          {
                            "title": "Dune", "language": {"name": "French", "code2": "fr"},
                            "provider": "subdl", "action": 4, "raw_timestamp": "$twoDaysAgo", "radarrId": 5
                          }
                        ]
                    }"""
                )
            )
        )
    }

    private fun runJob(identity: String) {
        val run = RestAssured.given()
            .`when`().post("/api/admin/jobs/$identity/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(run.getString("status")).isEqualTo("SUCCESS")
    }

    private fun getTimeline(page: Int = 0, pageSize: Int = 100): JsonPath =
        RestAssured.given()
            .queryParam("page", page)
            .queryParam("pageSize", pageSize)
            .`when`().get("/api/timeline")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `radarr sync imports downloaded movies in the timeline`() {
        runJob("radarr-downloads-sync")

        val items = getTimeline().getList<Map<String, Any>>("items")
        assertThat(items).hasSize(1)
        assertThat(items.first()["eventType"]).isEqualTo("movie_downloaded")
        assertThat(items.first()["title"]).isEqualTo("Dune (2024)")
        @Suppress("UNCHECKED_CAST")
        val details = items.first()["details"] as Map<String, String>
        assertThat(details["quality"]).isEqualTo("WEBDL-1080p")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `sonarr sync imports one timeline event per episode and skips non-import events`() {
        runJob("sonarr-downloads-sync")

        val items = getTimeline().getList<Map<String, Any>>("items")
        assertThat(items).hasSize(2)
        assertThat(items.map { it["eventType"] }).containsOnly("episode_downloaded")
        assertThat(items.map { it["title"] }).containsOnly("The Bear")
        val details = items.map { it["details"] as Map<*, *> }
        assertThat(details.map { it["episodeNumber"] }).containsExactlyInAnyOrder("1", "2")
        assertThat(details.map { it["seasonNumber"] }).containsOnly("2")
        assertThat(details.map { it["episodeTitle"] }).containsExactlyInAnyOrder("Premier", "Second")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `lidarr sync imports downloaded albums and skips per-track events`() {
        runJob("lidarr-downloads-sync")

        val items = getTimeline().getList<Map<String, Any>>("items")
        assertThat(items).hasSize(1)
        assertThat(items.first()["eventType"]).isEqualTo("album_downloaded")
        assertThat(items.first()["title"]).isEqualTo("Random Access Memories")
        @Suppress("UNCHECKED_CAST")
        val details = items.first()["details"] as Map<String, String>
        assertThat(details["artist"]).isEqualTo("Daft Punk")
        assertThat(details["quality"]).isEqualTo("FLAC")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `bazarr sync imports downloaded subtitles for episodes and movies`() {
        runJob("bazarr-downloads-sync")

        val items = getTimeline().getList<Map<String, Any>>("items")
        // The action=0 episode item must be filtered out
        assertThat(items).hasSize(2)
        assertThat(items.map { it["eventType"] }).containsOnly("subtitles_downloaded")
        assertThat(items.map { it["title"] }).containsExactlyInAnyOrder("The Bear", "Dune")
        val episodeDetails = items.first { it["title"] == "The Bear" }["details"] as Map<*, *>
        assertThat(episodeDetails["language"]).isEqualTo("French")
        assertThat(episodeDetails["provider"]).isEqualTo("opensubtitles")
        assertThat(episodeDetails["seasonNumber"]).isEqualTo("2")
        assertThat(episodeDetails["episodeNumber"]).isEqualTo("1")
        val movieDetails = items.first { it["title"] == "Dune" }["details"] as Map<*, *>
        assertThat(movieDetails["provider"]).isEqualTo("subdl")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `running the same sync twice does not duplicate timeline events`() {
        runJob("radarr-downloads-sync")
        runJob("sonarr-downloads-sync")
        runJob("lidarr-downloads-sync")
        runJob("bazarr-downloads-sync")
        val totalCount = getTimeline().getLong("totalCount")

        runJob("radarr-downloads-sync")
        runJob("sonarr-downloads-sync")
        runJob("lidarr-downloads-sync")
        runJob("bazarr-downloads-sync")

        assertThat(getTimeline().getLong("totalCount")).isEqualTo(totalCount)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `first sync requests history over the configured backfill window`() {
        runJob("radarr-downloads-sync")

        val request = wireMock.find(
            getRequestedFor(urlPathEqualTo("/api/v3/history/since"))
                .withQueryParam("includeMovie", equalTo("true"))
        ).first()
        val since = OffsetDateTime.parse(request.queryParameter("date").firstValue()).toInstant()
        val expected = Instant.now().minus(30, ChronoUnit.DAYS)
        assertThat(since).isBetween(expected.minus(1, ChronoUnit.HOURS), expected.plus(1, ChronoUnit.HOURS))
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `timeline is paginated and sorted by most recent download first`() {
        runJob("radarr-downloads-sync")
        runJob("sonarr-downloads-sync")

        val firstPage = getTimeline(page = 0, pageSize = 1)
        assertThat(firstPage.getList<Any>("items")).hasSize(1)
        assertThat(firstPage.getInt("pageSize")).isEqualTo(1)
        assertThat(firstPage.getLong("totalCount")).isEqualTo(3)
        assertThat(firstPage.getInt("totalPages")).isEqualTo(3)

        // Movie and first episode are from yesterday, second episode is older and must come last
        val lastPage = getTimeline(page = 2, pageSize = 1)
        val lastItem = lastPage.getList<Map<String, Any>>("items").first()
        assertThat(lastItem["title"]).isEqualTo("The Bear")
        assertThat((lastItem["details"] as Map<*, *>)["episodeNumber"]).isEqualTo("2")
    }

    @Test
    fun `anonymous user cannot read the timeline`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get("/api/timeline")
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
