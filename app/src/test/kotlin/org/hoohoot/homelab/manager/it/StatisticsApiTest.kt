package org.hoohoot.homelab.manager.it

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.PlaybackSessionSeed
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

@QuarkusTest
internal class StatisticsApiTest {

    @BeforeEach
    fun setUp() {
        PlaybackSessionSeed.deleteAll()
        listOf("2020-06-10", "2020-06-11").forEachIndexed { day, date ->
            repeat(3) { index ->
                val episode = day * 3 + index + 1
                PlaybackSessionSeed.insertSession(
                    userName = "stats-marie",
                    itemId = "stats-show-e$episode",
                    itemName = "Episode $episode",
                    mediaType = MediaType.EPISODE,
                    seriesId = "stats-show",
                    seriesName = "Stats Show",
                    seasonNumber = 1,
                    episodeNumber = episode,
                    startedAt = LocalDateTime.parse("${date}T12:${index}0:00"),
                    durationSeconds = 1800,
                    completed = true,
                    platform = "WEB",
                )
            }
        }
        PlaybackSessionSeed.insertSession(
            userName = "stats-marie",
            itemId = "stats-movie",
            itemName = "Stats Movie",
            mediaType = MediaType.MOVIE,
            startedAt = LocalDateTime.parse("2020-06-12T18:00:00"),
            durationSeconds = 7200,
            completed = true,
            platform = "ANDROID_TV",
        )
        PlaybackSessionSeed.insertSession(
            userName = "stats-jean",
            itemId = "stats-movie",
            itemName = "Stats Movie",
            mediaType = MediaType.MOVIE,
            startedAt = LocalDateTime.parse("2020-06-12T19:00:00"),
            durationSeconds = 3600,
            completed = false,
            platform = "ANDROID_TV",
        )
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `summary aggregates watch time, completed items and peak hour`() {
        val summary = RestAssured.given()
            .`when`().get("/api/statistics/summary?period=ALL_TIME")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(summary.getLong("totalWatchTimeSeconds")).isEqualTo(21600L)
        assertThat(summary.getLong("playCount")).isEqualTo(8L)
        assertThat(summary.getLong("completedItems")).isEqualTo(7L)
        assertThat(summary.getInt("peakHour")).isEqualTo(14)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `top users are ordered by watch time`() {
        val users = RestAssured.given()
            .`when`().get("/api/statistics/top-users?period=ALL_TIME")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(users.getString("[0].userName")).isEqualTo("stats-marie")
        assertThat(users.getLong("[0].watchTimeSeconds")).isEqualTo(18000L)
        assertThat(users.getLong("[0].itemsWatched")).isEqualTo(7L)
        assertThat(users.getLong("[0].playCount")).isEqualTo(7L)
        assertThat(users.getString("[1].userName")).isEqualTo("stats-jean")
        assertThat(users.getLong("[1].watchTimeSeconds")).isEqualTo(3600L)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `top series expose completion rate and binge score`() {
        val series = RestAssured.given()
            .`when`().get("/api/statistics/top-media?period=ALL_TIME&type=SERIES")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(series.getString("[0].name")).isEqualTo("Stats Show")
        assertThat(series.getLong("[0].plays")).isEqualTo(6L)
        assertThat(series.getLong("[0].watchTimeSeconds")).isEqualTo(10800L)
        assertThat(series.getLong("[0].uniqueViewers")).isEqualTo(1L)
        assertThat(series.getDouble("[0].completionRate")).isEqualTo(100.0)
        assertThat(series.getInt("[0].bingeScore")).isEqualTo(50)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `top movies expose plays and completion rate without binge score`() {
        val movies = RestAssured.given()
            .`when`().get("/api/statistics/top-media?period=ALL_TIME&type=MOVIE")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(movies.getString("[0].name")).isEqualTo("Stats Movie")
        assertThat(movies.getLong("[0].plays")).isEqualTo(2L)
        assertThat(movies.getLong("[0].watchTimeSeconds")).isEqualTo(10800L)
        assertThat(movies.getLong("[0].uniqueViewers")).isEqualTo(2L)
        assertThat(movies.getDouble("[0].completionRate")).isEqualTo(50.0)
        assertThat(movies.getString("[0].bingeScore")).isNull()
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `top media is sorted by plays by default and honors the sort parameters`() {
        PlaybackSessionSeed.insertSession(
            userName = "stats-luc",
            itemId = "stats-movie-b",
            itemName = "Stats Movie B",
            mediaType = MediaType.MOVIE,
            startedAt = LocalDateTime.parse("2020-06-13T10:00:00"),
            durationSeconds = 1800,
            completed = true,
        )

        val byPlays = RestAssured.given()
            .`when`().get("/api/statistics/top-media?period=ALL_TIME&type=MOVIE")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(byPlays.getString("[0].name")).isEqualTo("Stats Movie")
        assertThat(byPlays.getString("[1].name")).isEqualTo("Stats Movie B")

        val byCompletion = RestAssured.given()
            .`when`().get("/api/statistics/top-media?period=ALL_TIME&type=MOVIE&sort=COMPLETION_RATE&order=DESC")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(byCompletion.getString("[0].name")).isEqualTo("Stats Movie B")
        assertThat(byCompletion.getString("[1].name")).isEqualTo("Stats Movie")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `weekday activity fills the 7 days and counts plays in local timezone`() {
        val weekdays = RestAssured.given()
            .`when`().get("/api/statistics/activity-by-weekday?period=ALL_TIME")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(weekdays.getList<Any>("$")).hasSize(7)
        assertThat(weekdays.getLong("[2].plays")).isEqualTo(3L)
        assertThat(weekdays.getLong("[3].plays")).isEqualTo(3L)
        assertThat(weekdays.getLong("[4].plays")).isEqualTo(2L)
        assertThat(weekdays.getLong("[0].plays")).isEqualTo(0L)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `hourly activity fills the 24 hours in local timezone`() {
        val hours = RestAssured.given()
            .`when`().get("/api/statistics/activity-by-hour?period=ALL_TIME")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(hours.getList<Any>("$")).hasSize(24)
        assertThat(hours.getLong("[14].plays")).isEqualTo(6L)
        assertThat(hours.getLong("[20].plays")).isEqualTo(1L)
        assertThat(hours.getLong("[21].plays")).isEqualTo(1L)
        assertThat(hours.getLong("[0].plays")).isEqualTo(0L)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `platform breakdown groups sessions by platform`() {
        val platforms = RestAssured.given()
            .`when`().get("/api/statistics/platforms?period=ALL_TIME")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(platforms.getString("[0].platform")).isEqualTo("WEB")
        assertThat(platforms.getLong("[0].plays")).isEqualTo(6L)
        assertThat(platforms.getString("[1].platform")).isEqualTo("ANDROID_TV")
        assertThat(platforms.getLong("[1].plays")).isEqualTo(2L)
        assertThat(platforms.getLong("[1].watchTimeSeconds")).isEqualTo(10800L)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `plays over time uses month buckets for all time and fills gaps from first play`() {
        val overTime = RestAssured.given()
            .`when`().get("/api/statistics/plays-over-time?period=ALL_TIME")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(overTime.getString("granularity")).isEqualTo("MONTH")
        assertThat(overTime.getString("points[0].bucketStart")).startsWith("2020-06-01T00:00")
        assertThat(overTime.getLong("points[0].plays")).isEqualTo(8L)
        assertThat(overTime.getLong("points[1].plays")).isEqualTo(0L)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `plays over time uses hour buckets for today`() {
        val overTime = RestAssured.given()
            .`when`().get("/api/statistics/plays-over-time?period=TODAY")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(overTime.getString("granularity")).isEqualTo("HOUR")
        assertThat(overTime.getList<Any>("points")).hasSize(24)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `now playing returns the in-memory sessions`() {
        RestAssured.given()
            .`when`().get("/api/statistics/now-playing")
            .then().statusCode(Response.Status.OK.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `import is forbidden for non admins`() {
        val file = File.createTempFile("backup", ".json").apply { writeText("[]") }
        RestAssured.given()
            .multiPart("file", file)
            .`when`().post("/api/admin/statistics/import")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }
}
