package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.vertx.VertxContextSupport
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import org.hoohoot.homelab.manager.portal.persistence.MediaDownloadEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
internal class CorrectorTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CorrectorWorkflowEntity.deleteAll().chain { _ -> MediaDownloadEntity.deleteAll() }
            }
        }

        wireMock.resetMappings()
        wireMock.resetRequests()
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(
                okJson(
                    """[
                        {
                          "id": 1, "title": "Dune: Part Two", "year": 2024, "hasFile": true,
                          "overview": "Paul Atreides mène la révolte.",
                          "images": [{"coverType": "poster", "remoteUrl": "https://img/dune2.jpg"}],
                          "movieFile": {
                            "quality": {"quality": {"name": "WEBDL-1080p"}},
                            "languages": [{"id": 1, "name": "English"}]
                          }
                        },
                        {"id": 2, "title": "Oppenheimer", "year": 2023},
                        {"id": 3, "title": "Un Prophète", "year": 2009}
                    ]"""
                )
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/release")).withQueryParam("movieId", equalTo("1")).willReturn(
                okJson(
                    """[
                        {
                          "guid": "release-vo", "indexerId": 2, "indexer": "Sharewood",
                          "title": "Dune.Part.Two.2024.1080p.WEB.H264-FLUX",
                          "quality": {"quality": {"name": "WEBDL-1080p"}},
                          "languages": [{"id": 1, "name": "English"}],
                          "size": 9663676416, "seeders": 412, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-multi", "indexerId": 1, "indexer": "YGG",
                          "title": "Dune.Part.Two.2024.MULTI.1080p.WEB.H264-Slay3R",
                          "quality": {"quality": {"name": "WEBDL-1080p"}},
                          "languages": [{"id": 1, "name": "English"}],
                          "size": 8589934592, "seeders": 145, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-truefrench", "indexerId": 1, "indexer": "YGG",
                          "title": "Dune.Part.Two.2024.TRUEFRENCH.720p.BluRay.x264-Fr3nchy",
                          "quality": {"quality": {"name": "Bluray-720p"}},
                          "languages": [], "size": 4294967296, "seeders": 52, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-french-language", "indexerId": 1, "indexer": "YGG",
                          "title": "Dune.Part.Two.2024.1080p.BluRay.x264-NoTag",
                          "quality": {"quality": {"name": "Bluray-1080p"}},
                          "languages": [{"id": 2, "name": "French"}],
                          "size": 10737418240, "seeders": 30, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-vostfr", "indexerId": 2, "indexer": "Sharewood",
                          "title": "Dune.Part.Two.2024.VOSTFR.1080p.WEB.x264-SubTeam",
                          "quality": {"quality": {"name": "WEBDL-1080p"}},
                          "languages": [{"id": 1, "name": "English"}],
                          "size": 7516192768, "seeders": 88, "rejected": false, "rejections": []
                        }
                    ]"""
                )
            )
        )
        wireMock.register(
            post(urlPathEqualTo("/api/v3/release")).willReturn(okJson("""{"guid": "release-multi", "indexerId": 1}"""))
        )
    }

    private fun createWorkflow(): JsonPath =
        RestAssured.given().contentType(ContentType.JSON).body("""{"mediaType": "movie"}""")
            .`when`().post("/api/corrector/workflows")
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath()

    private fun selectMovie(id: String, radarrMovieId: Int = 1): JsonPath =
        RestAssured.given().contentType(ContentType.JSON).body("""{"radarrMovieId": $radarrMovieId}""")
            .`when`().post("/api/corrector/workflows/$id/movie")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun selectProblem(id: String): JsonPath =
        RestAssured.given().contentType(ContentType.JSON).body("""{"problemType": "vo_should_be_french"}""")
            .`when`().post("/api/corrector/workflows/$id/problem")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun grabRelease(id: String): JsonPath =
        RestAssured.given().contentType(ContentType.JSON)
            .body("""{"guid": "release-multi", "indexerId": 1, "title": "Dune.Part.Two.2024.MULTI.1080p.WEB.H264-Slay3R", "indexer": "YGG", "quality": "WEBDL-1080p", "size": 8589934592}""")
            .`when`().post("/api/corrector/workflows/$id/grab")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun getWorkflow(id: String): JsonPath =
        RestAssured.given()
            .`when`().get("/api/corrector/workflows/$id")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun runToAwaitingImport(): String {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)
        grabRelease(id)
        return id
    }

    private fun registerRadarrImportHistory(movieId: Int, importedAt: Instant) {
        wireMock.register(
            get(urlPathEqualTo("/api/v3/history/since"))
                .withQueryParam("includeMovie", equalTo("true"))
                .willReturn(
                    okJson(
                        """[
                            {
                              "id": 1001, "movieId": $movieId, "eventType": "downloadFolderImported", "date": "$importedAt",
                              "quality": {"quality": {"name": "WEBDL-1080p"}},
                              "movie": {"id": $movieId, "title": "Dune: Part Two", "year": 2024}
                            }
                        ]"""
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

    private fun insertWorkflowFor(username: String): UUID {
        val id = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                val entity = CorrectorWorkflowEntity()
                entity.id = id
                entity.username = username
                entity.mediaType = CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE
                entity.status = CorrectorWorkflowEntity.STATUS_IN_PROGRESS
                entity.createdAt = LocalDateTime.now()
                entity.updatedAt = LocalDateTime.now()
                entity.persist<CorrectorWorkflowEntity>()
            }
        }
        return id
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `creating a workflow starts at the movie selection step`() {
        val workflow = createWorkflow()

        assertThat(workflow.getString("mediaType")).isEqualTo("movie")
        assertThat(workflow.getString("status")).isEqualTo("IN_PROGRESS")
        assertThat(workflow.getString("currentStep")).isEqualTo("SELECT_MOVIE")

        val list = RestAssured.given()
            .`when`().get("/api/corrector/workflows")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")
        assertThat(list).hasSize(1)
        assertThat(list.first()["id"]).isEqualTo(workflow.getString("id"))
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `creating a workflow for a series is rejected`() {
        RestAssured.given().contentType(ContentType.JSON).body("""{"mediaType": "series"}""")
            .`when`().post("/api/corrector/workflows")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `users cannot see or access workflows of other users`() {
        val aliceWorkflowId = insertWorkflowFor("alice")

        val list = RestAssured.given()
            .`when`().get("/api/corrector/workflows")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Any>("")
        assertThat(list).isEmpty()

        RestAssured.given()
            .`when`().get("/api/corrector/workflows/$aliceWorkflowId")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `movie search filters the radarr library ignoring case and accents`() {
        val results = RestAssured.given().queryParam("query", "prophete")
            .`when`().get("/api/corrector/movies")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")

        assertThat(results).hasSize(1)
        assertThat(results.first()["title"]).isEqualTo("Un Prophète")
        assertThat(results.first()["radarrMovieId"]).isEqualTo(3)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a movie snapshots its info and moves to the problem step`() {
        val id = createWorkflow().getString("id")

        val workflow = selectMovie(id)

        assertThat(workflow.getString("currentStep")).isEqualTo("SELECT_PROBLEM")
        assertThat(workflow.getString("movie.title")).isEqualTo("Dune: Part Two")
        assertThat(workflow.getInt("movie.year")).isEqualTo(2024)
        assertThat(workflow.getString("movie.posterUrl")).isEqualTo("https://img/dune2.jpg")
        assertThat(workflow.getString("movie.currentQuality")).isEqualTo("WEBDL-1080p")
        assertThat(workflow.getList<String>("movie.currentLanguages")).containsExactly("English")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `releases are annotated as french from languages or title but not for vostfr`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)

        val releases = RestAssured.given()
            .`when`().get("/api/corrector/workflows/$id/releases")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")

        assertThat(releases).hasSize(5)
        val byGuid = releases.associateBy { it["guid"] }
        // MULTI dans le titre = FR, même si Radarr ne parse pas de langue French
        assertThat(byGuid["release-multi"]!!["isFrench"]).isEqualTo(true)
        assertThat(byGuid["release-truefrench"]!!["isFrench"]).isEqualTo(true)
        assertThat(byGuid["release-french-language"]!!["isFrench"]).isEqualTo(true)
        assertThat(byGuid["release-vostfr"]!!["isFrench"]).isEqualTo(false)
        assertThat(byGuid["release-vo"]!!["isFrench"]).isEqualTo(false)
        // Tri : françaises d'abord, puis par seeders
        assertThat(releases.map { it["guid"] })
            .containsExactly("release-multi", "release-truefrench", "release-french-language", "release-vo", "release-vostfr")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `grabbing a release pushes it to radarr and awaits import`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)

        val workflow = grabRelease(id)

        assertThat(workflow.getString("status")).isEqualTo("AWAITING_IMPORT")
        assertThat(workflow.getString("currentStep")).isEqualTo("AWAITING_IMPORT")
        assertThat(workflow.getString("grabbedRelease.title"))
            .isEqualTo("Dune.Part.Two.2024.MULTI.1080p.WEB.H264-Slay3R")
        wireMock.verifyThat(
            postRequestedFor(urlPathEqualTo("/api/v3/release"))
                .withRequestBody(matchingJsonPath("$.guid", equalTo("release-multi")))
                .withRequestBody(matchingJsonPath("$.indexerId", equalTo("1")))
        )
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `radarr sync completes awaiting workflows when the movie is imported after the grab`() {
        val id = runToAwaitingImport()
        registerRadarrImportHistory(movieId = 1, importedAt = Instant.now().plus(1, ChronoUnit.HOURS))

        runJob("radarr-downloads-sync")

        val workflow = getWorkflow(id)
        assertThat(workflow.getString("status")).isEqualTo("COMPLETED")
        assertThat(workflow.getString("completedAt")).isNotNull()
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `radarr sync ignores imports that happened before the grab`() {
        val id = runToAwaitingImport()
        // L'import VO d'origine réapparaît dans l'historique à cause de l'overlap du watermark
        registerRadarrImportHistory(movieId = 1, importedAt = Instant.now().minus(2, ChronoUnit.HOURS))

        runJob("radarr-downloads-sync")

        assertThat(getWorkflow(id).getString("status")).isEqualTo("AWAITING_IMPORT")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `grabbing without selecting a problem first is rejected`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)

        RestAssured.given().contentType(ContentType.JSON).body("""{"guid": "release-multi", "indexerId": 1}""")
            .`when`().post("/api/corrector/workflows/$id/grab")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a movie without radarrMovieId is rejected`() {
        val id = createWorkflow().getString("id")

        RestAssured.given().contentType(ContentType.JSON).body("""{}""")
            .`when`().post("/api/corrector/workflows/$id/movie")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `grabbing without a guid is rejected`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)

        RestAssured.given().contentType(ContentType.JSON).body("""{"indexerId": 1}""")
            .`when`().post("/api/corrector/workflows/$id/grab")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a problem without a movie is rejected`() {
        val id = createWorkflow().getString("id")

        RestAssured.given().contentType(ContentType.JSON).body("""{"problemType": "vo_should_be_french"}""")
            .`when`().post("/api/corrector/workflows/$id/problem")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `an unfinished workflow can be abandoned`() {
        val id = createWorkflow().getString("id")

        val workflow = RestAssured.given()
            .`when`().post("/api/corrector/workflows/$id/abandon")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(workflow.getString("status")).isEqualTo("ABANDONED")
    }

    @Test
    fun `anonymous users cannot access the corrector`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get("/api/corrector/workflows")
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
