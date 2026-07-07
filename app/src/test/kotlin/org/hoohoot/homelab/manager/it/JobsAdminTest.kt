package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.path.json.JsonPath
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class JobsAdminTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        wireMock.resetMappings()
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(
                okJson("""[{"id": 1, "title": "Dune"}, {"id": 2, "title": "Oppenheimer"}]""")
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/series")).willReturn(
                okJson("""[{"id": 10, "title": "The Bear", "statistics": {"episodeFileCount": 28}}]""")
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/diskspace")).willReturn(
                okJson("""[{"path": "/data", "label": "data", "freeSpace": 500, "totalSpace": 2000}]""")
            )
        )
    }

    private fun listJobs(): JsonPath =
        RestAssured.given()
            .`when`().get("/api/admin/jobs")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun job(jobs: JsonPath, identity: String): Map<String, Any?>? =
        jobs.getList<Map<String, Any?>>("$").firstOrNull { it["identity"] == identity }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin can list all scheduled jobs with their metadata`() {
        val jobs = listJobs()

        val identities = jobs.getList<String>("identity")
        assertThat(identities).contains("radarr-sync", "sonarr-sync", "notification-cleanup", "weekly-report")

        val radarrSync = job(jobs, "radarr-sync")!!
        assertThat(radarrSync["displayName"]).isEqualTo("Synchronisation des stats Radarr")
        assertThat(radarrSync["schedule"]).isEqualTo("every 15m")
        assertThat(radarrSync["nextFireTime"]).isNotNull
        assertThat(radarrSync["runnable"]).isEqualTo(true)
        assertThat(radarrSync["paused"]).isEqualTo(false)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `manual run persists a snapshot and stats are then served from it`() {
        val radarrRun = RestAssured.given()
            .`when`().post("/api/admin/jobs/radarr-sync/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(radarrRun.getString("status")).isEqualTo("SUCCESS")
        assertThat(radarrRun.getBoolean("manual")).isTrue

        RestAssured.given()
            .`when`().post("/api/admin/jobs/sonarr-sync/run")
            .then().statusCode(Response.Status.OK.statusCode)

        // Different live values from now on: /api/stats must keep serving the persisted snapshot
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(okJson("""[{"id": 9, "title": "Alien"}]"""))
        )

        val stats = RestAssured.given()
            .`when`().get("/api/stats")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(stats.getInt("movieCount")).isEqualTo(2)
        assertThat(stats.getInt("seriesCount")).isEqualTo(1)
        assertThat(stats.getInt("episodeCount")).isEqualTo(28)
        // Radarr and Sonarr snapshots both report /data: the merge dedupes it
        assertThat(stats.getLong("diskTotalBytes")).isEqualTo(2000)
        assertThat(stats.getLong("diskFreeBytes")).isEqualTo(500)
        assertThat(stats.getLong("diskUsedBytes")).isEqualTo(1500)

        val lastExecution = job(listJobs(), "radarr-sync")!!["lastExecution"]
        @Suppress("UNCHECKED_CAST")
        assertThat((lastExecution as Map<String, Any?>)["status"]).isEqualTo("SUCCESS")
        assertThat(lastExecution["manual"]).isEqualTo(true)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `failed manual run is recorded as a failure with its error`() {
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(aResponse().withStatus(500))
        )

        val run = RestAssured.given()
            .`when`().post("/api/admin/jobs/radarr-sync/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(run.getString("status")).isEqualTo("FAILURE")
        assertThat(run.getString("error")).isNotBlank

        val lastExecution = job(listJobs(), "radarr-sync")!!["lastExecution"]
        @Suppress("UNCHECKED_CAST")
        assertThat((lastExecution as Map<String, Any?>)["status"]).isEqualTo("FAILURE")
        assertThat(lastExecution["error"]).isNotNull
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin can pause and resume a job`() {
        RestAssured.given()
            .`when`().post("/api/admin/jobs/weekly-report/pause")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
        assertThat(job(listJobs(), "weekly-report")!!["paused"]).isEqualTo(true)

        RestAssured.given()
            .`when`().post("/api/admin/jobs/weekly-report/resume")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
        assertThat(job(listJobs(), "weekly-report")!!["paused"]).isEqualTo(false)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `unknown job returns 404`() {
        RestAssured.given().`when`().post("/api/admin/jobs/unknown-job/run")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
        RestAssured.given().`when`().post("/api/admin/jobs/unknown-job/pause")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
        RestAssured.given().`when`().post("/api/admin/jobs/unknown-job/resume")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `non-admin user cannot access jobs administration`() {
        RestAssured.given().`when`().get("/api/admin/jobs")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
        RestAssured.given().`when`().post("/api/admin/jobs/radarr-sync/run")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
        RestAssured.given().`when`().post("/api/admin/jobs/radarr-sync/pause")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    fun `anonymous user cannot access jobs administration`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get("/api/admin/jobs")
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
