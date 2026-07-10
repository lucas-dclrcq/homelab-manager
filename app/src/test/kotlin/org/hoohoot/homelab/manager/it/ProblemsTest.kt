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
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import org.hoohoot.homelab.manager.library.infra.MediaDownloadEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
internal class ProblemsTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                ProblemWorkflowEntity.deleteAll().chain { _ -> MediaDownloadEntity.deleteAll() }
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
                          "overview": "Paul Atreides mène la révolte.", "qualityProfileId": 4,
                          "images": [{"coverType": "poster", "remoteUrl": "https://img/dune2.jpg"}],
                          "movieFile": {
                            "quality": {"quality": {"name": "Bluray-720p"}},
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
                          "languages": [{"id": 1, "name": "English"}], "protocol": "torrent",
                          "size": 9663676416, "seeders": 412, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-multi", "indexerId": 1, "indexer": "YGG",
                          "title": "Dune.Part.Two.2024.MULTI.1080p.WEB.H264-Slay3R",
                          "quality": {"quality": {"name": "WEBDL-1080p"}},
                          "languages": [{"id": 1, "name": "English"}], "protocol": "torrent",
                          "size": 8589934592, "seeders": 145,
                          "rejected": true, "rejections": ["Quality profile does not allow upgrades"]
                        },
                        {
                          "guid": "release-truefrench", "indexerId": 1, "indexer": "YGG",
                          "title": "Dune.Part.Two.2024.TRUEFRENCH.720p.BluRay.x264-Fr3nchy",
                          "quality": {"quality": {"name": "Bluray-720p"}},
                          "languages": [], "protocol": "torrent",
                          "size": 4294967296, "seeders": 52, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-french-language", "indexerId": 1, "indexer": "YGG",
                          "title": "Dune.Part.Two.2024.1080p.BluRay.x264-NoTag",
                          "quality": {"quality": {"name": "Bluray-1080p"}},
                          "languages": [{"id": 2, "name": "French"}], "protocol": "usenet",
                          "size": 10737418240, "seeders": 30, "rejected": false, "rejections": []
                        },
                        {
                          "guid": "release-vostfr", "indexerId": 2, "indexer": "Sharewood",
                          "title": "Dune.Part.Two.2024.VOSTFR.1080p.WEB.x264-SubTeam",
                          "quality": {"quality": {"name": "WEBDL-1080p"}},
                          "languages": [{"id": 1, "name": "English"}], "protocol": "torrent",
                          "size": 7516192768, "seeders": 88, "rejected": false, "rejections": []
                        }
                    ]"""
                )
            )
        )
        // Oppenheimer n'a pas de fichier : la contrainte de résolution est levée
        wireMock.register(
            get(urlPathEqualTo("/api/v3/release")).withQueryParam("movieId", equalTo("2")).willReturn(
                okJson(
                    """[
                        {
                          "guid": "release-oppenheimer-multi", "indexerId": 1, "indexer": "YGG",
                          "title": "Oppenheimer.2023.MULTI.720p.WEB.H264-Team",
                          "quality": {"quality": {"name": "WEBDL-720p"}},
                          "languages": [], "protocol": "torrent",
                          "size": 4294967296, "seeders": 77, "rejected": false, "rejections": []
                        }
                    ]"""
                )
            )
        )
        wireMock.register(
            post(urlPathEqualTo("/api/v3/release")).willReturn(okJson("""{"guid": "release-multi", "indexerId": 1}"""))
        )
        // Profil « HD-1080p » : cutoff = groupe « WEB 1080p » (cas réel du profil Radarr par défaut)
        wireMock.register(
            get(urlPathEqualTo("/api/v3/qualityprofile")).willReturn(
                okJson(
                    """[
                        {
                          "id": 4, "name": "HD-1080p", "cutoff": 1003,
                          "items": [
                            {"quality": {"id": 1, "name": "SDTV", "resolution": 480}, "allowed": false},
                            {"id": 1002, "name": "WEB 720p", "allowed": false, "items": [
                                {"quality": {"id": 14, "name": "WEBRip-720p", "resolution": 720}, "allowed": false},
                                {"quality": {"id": 5, "name": "WEBDL-720p", "resolution": 720}, "allowed": false}
                            ]},
                            {"quality": {"id": 6, "name": "Bluray-720p", "resolution": 720}, "allowed": false},
                            {"id": 1003, "name": "WEB 1080p", "allowed": true, "items": [
                                {"quality": {"id": 3, "name": "WEBDL-1080p", "resolution": 1080}, "allowed": true},
                                {"quality": {"id": 15, "name": "WEBRip-1080p", "resolution": 1080}, "allowed": true}
                            ]},
                            {"quality": {"id": 7, "name": "Bluray-1080p", "resolution": 1080}, "allowed": true}
                          ]
                        }
                    ]"""
                )
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/series")).willReturn(
                okJson(
                    """[
                        {
                          "id": 10, "title": "Severance", "year": 2022,
                          "overview": "Des employés au cerveau coupé en deux.",
                          "images": [{"coverType": "poster", "remoteUrl": "https://img/severance.jpg"}]
                        },
                        {"id": 11, "title": "Dark", "year": 2017}
                    ]"""
                )
            )
        )
    }

    private fun createWorkflow(mediaType: String = "movie"): JsonPath =
        RestAssured.given().contentType(ContentType.JSON).body("""{"mediaType": "$mediaType"}""")
            .`when`().post("/api/problems/workflows")
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath()

    private fun selectMovie(id: String, radarrMovieId: Int = 1): JsonPath =
        RestAssured.given().contentType(ContentType.JSON).body("""{"radarrMovieId": $radarrMovieId}""")
            .`when`().post("/api/problems/workflows/$id/movie")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun selectProblem(
        id: String,
        problemType: String = "vo_should_be_french",
        description: String? = null,
    ): JsonPath {
        val descriptionJson = description?.let { """, "description": "$it"""" } ?: ""
        return RestAssured.given().contentType(ContentType.JSON)
            .body("""{"problemType": "$problemType"$descriptionJson}""")
            .`when`().post("/api/problems/workflows/$id/problem")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
    }

    private fun grabRelease(id: String): JsonPath =
        RestAssured.given().contentType(ContentType.JSON)
            .body("""{"guid": "release-multi", "indexerId": 1, "title": "Dune.Part.Two.2024.MULTI.1080p.WEB.H264-Slay3R", "indexer": "YGG", "quality": "WEBDL-1080p", "size": 8589934592}""")
            .`when`().post("/api/problems/workflows/$id/grab")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun getWorkflow(id: String): JsonPath =
        RestAssured.given()
            .`when`().get("/api/problems/workflows/$id")
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

    private fun insertWorkflowFor(
        username: String,
        status: String = ProblemWorkflowEntity.STATUS_IN_PROGRESS,
    ): UUID {
        val id = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                val entity = ProblemWorkflowEntity()
                entity.id = id
                entity.username = username
                entity.mediaType = ProblemWorkflowEntity.MEDIA_TYPE_MOVIE
                entity.status = status
                entity.createdAt = LocalDateTime.now()
                entity.updatedAt = LocalDateTime.now()
                entity.persist<ProblemWorkflowEntity>()
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
        assertThat(workflow.getString("currentStep")).isEqualTo("SELECT_MEDIA")

        val list = RestAssured.given()
            .`when`().get("/api/problems/workflows")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")
        assertThat(list).hasSize(1)
        assertThat(list.first()["id"]).isEqualTo(workflow.getString("id"))
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `creating a workflow with an unknown media type is rejected`() {
        RestAssured.given().contentType(ContentType.JSON).body("""{"mediaType": "podcast"}""")
            .`when`().post("/api/problems/workflows")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `a tv workflow starts at the media selection step`() {
        val workflow = createWorkflow(mediaType = "tv")

        assertThat(workflow.getString("mediaType")).isEqualTo("tv")
        assertThat(workflow.getString("status")).isEqualTo("IN_PROGRESS")
        assertThat(workflow.getString("currentStep")).isEqualTo("SELECT_MEDIA")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `users cannot see or access workflows of other users`() {
        val aliceWorkflowId = insertWorkflowFor("alice")

        val list = RestAssured.given()
            .`when`().get("/api/problems/workflows")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Any>("")
        assertThat(list).isEmpty()

        RestAssured.given()
            .`when`().get("/api/problems/workflows/$aliceWorkflowId")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `movie search filters the radarr library ignoring case and accents`() {
        val results = RestAssured.given().queryParam("query", "prophete")
            .`when`().get("/api/problems/movies")
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
        assertThat(workflow.getString("media.title")).isEqualTo("Dune: Part Two")
        assertThat(workflow.getInt("media.year")).isEqualTo(2024)
        assertThat(workflow.getString("media.posterUrl")).isEqualTo("https://img/dune2.jpg")
        assertThat(workflow.getString("media.currentQuality")).isEqualTo("Bluray-720p")
        assertThat(workflow.getList<String>("media.currentLanguages")).containsExactly("English")
        // Résolution voulue issue du profil Radarr (cutoff), pas celle du fichier 720p actuel
        assertThat(workflow.getString("media.desiredResolution")).isEqualTo("1080")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `series search filters the sonarr library`() {
        val results = RestAssured.given().queryParam("query", "sever")
            .`when`().get("/api/problems/series")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")

        assertThat(results).hasSize(1)
        assertThat(results.first()["title"]).isEqualTo("Severance")
        assertThat(results.first()["sonarrSeriesId"]).isEqualTo(10)
        assertThat(results.first()["posterUrl"]).isEqualTo("https://img/severance.jpg")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a series snapshots its info and moves to the problem step`() {
        val id = createWorkflow(mediaType = "tv").getString("id")

        val workflow = RestAssured.given().contentType(ContentType.JSON).body("""{"sonarrSeriesId": 10}""")
            .`when`().post("/api/problems/workflows/$id/series")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(workflow.getString("currentStep")).isEqualTo("SELECT_PROBLEM")
        assertThat(workflow.getString("media.title")).isEqualTo("Severance")
        assertThat(workflow.getInt("media.year")).isEqualTo(2022)
        assertThat(workflow.getString("media.posterUrl")).isEqualTo("https://img/severance.jpg")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a movie on a tv workflow is rejected`() {
        val id = createWorkflow(mediaType = "tv").getString("id")

        RestAssured.given().contentType(ContentType.JSON).body("""{"radarrMovieId": 1}""")
            .`when`().post("/api/problems/workflows/$id/movie")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `the vf problem is not available for series`() {
        val id = createWorkflow(mediaType = "tv").getString("id")
        RestAssured.given().contentType(ContentType.JSON).body("""{"sonarrSeriesId": 10}""")
            .`when`().post("/api/problems/workflows/$id/series")
            .then().statusCode(Response.Status.OK.statusCode)

        RestAssured.given().contentType(ContentType.JSON).body("""{"problemType": "vo_should_be_french"}""")
            .`when`().post("/api/problems/workflows/$id/problem")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `declaring an other problem reports it with its description`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)

        val workflow = selectProblem(id, problemType = "other", description = "Le fichier est corrompu à 1h12")

        assertThat(workflow.getString("status")).isEqualTo("REPORTED")
        assertThat(workflow.getString("currentStep")).isEqualTo("REPORTED")
        assertThat(workflow.getString("description")).isEqualTo("Le fichier est corrompu à 1h12")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `declaring an other problem on a series works too`() {
        val id = createWorkflow(mediaType = "tv").getString("id")
        RestAssured.given().contentType(ContentType.JSON).body("""{"sonarrSeriesId": 10}""")
            .`when`().post("/api/problems/workflows/$id/series")
            .then().statusCode(Response.Status.OK.statusCode)

        val workflow = selectProblem(id, problemType = "other", description = "La saison 2 est incomplète")

        assertThat(workflow.getString("status")).isEqualTo("REPORTED")
        assertThat(workflow.getString("description")).isEqualTo("La saison 2 est incomplète")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `an other problem without a description is rejected`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)

        RestAssured.given().contentType(ContentType.JSON).body("""{"problemType": "other"}""")
            .`when`().post("/api/problems/workflows/$id/problem")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `the reporter can resolve their own reported problem`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id, problemType = "other", description = "Mauvais ratio d'image")

        val workflow = RestAssured.given()
            .`when`().post("/api/problems/workflows/$id/resolve")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(workflow.getString("status")).isEqualTo("RESOLVED")
        assertThat(workflow.getString("completedAt")).isNotNull()
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `a completed workflow cannot be resolved or abandoned`() {
        val id = insertWorkflowFor("alice", status = ProblemWorkflowEntity.STATUS_COMPLETED)

        RestAssured.given()
            .`when`().post("/api/problems/workflows/$id/resolve")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
        RestAssured.given()
            .`when`().post("/api/problems/workflows/$id/abandon")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `a reported problem can be abandoned but not once resolved`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id, problemType = "other", description = "Pas le bon film")

        RestAssured.given()
            .`when`().post("/api/problems/workflows/$id/abandon")
            .then().statusCode(Response.Status.OK.statusCode)

        val resolvedId = insertWorkflowFor("alice", status = ProblemWorkflowEntity.STATUS_RESOLVED)
        RestAssured.given()
            .`when`().post("/api/problems/workflows/$resolvedId/abandon")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `releases are annotated as french from languages or title but not for vostfr`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)

        val releases = RestAssured.given()
            .`when`().get("/api/problems/workflows/$id/releases")
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
            .`when`().post("/api/problems/workflows/$id/grab")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a movie without radarrMovieId is rejected`() {
        val id = createWorkflow().getString("id")

        RestAssured.given().contentType(ContentType.JSON).body("""{}""")
            .`when`().post("/api/problems/workflows/$id/movie")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `grabbing without a guid is rejected`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)

        RestAssured.given().contentType(ContentType.JSON).body("""{"indexerId": 1}""")
            .`when`().post("/api/problems/workflows/$id/grab")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `selecting a problem without a movie is rejected`() {
        val id = createWorkflow().getString("id")

        RestAssured.given().contentType(ContentType.JSON).body("""{"problemType": "vo_should_be_french"}""")
            .`when`().post("/api/problems/workflows/$id/problem")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `an unfinished workflow can be abandoned`() {
        val id = createWorkflow().getString("id")

        val workflow = RestAssured.given()
            .`when`().post("/api/problems/workflows/$id/abandon")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(workflow.getString("status")).isEqualTo("ABANDONED")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `releases are recommended when torrent with a vf tag at the desired resolution even if rejected`() {
        val id = createWorkflow().getString("id")
        selectMovie(id)
        selectProblem(id)

        val releases = RestAssured.given()
            .`when`().get("/api/problems/workflows/$id/releases")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")

        val byGuid = releases.associateBy { it["guid"] }
        // Le fichier actuel est en 720p mais le profil demande du 1080p : on recommande l'upgrade
        // MULTI 1080p, même s'il est rejeté par les règles Radarr (taille/format).
        assertThat(byGuid["release-multi"]!!["isRecommended"]).isEqualTo(true)
        assertThat(byGuid["release-multi"]!!["rejected"]).isEqualTo(true)
        // 720p alors que la qualité demandée est 1080p
        assertThat(byGuid["release-truefrench"]!!["isRecommended"]).isEqualTo(false)
        // usenet, et pas de tag VF dans le titre
        assertThat(byGuid["release-french-language"]!!["isRecommended"]).isEqualTo(false)
        assertThat(byGuid["release-vo"]!!["isRecommended"]).isEqualTo(false)
        assertThat(byGuid["release-vostfr"]!!["isRecommended"]).isEqualTo(false)
        // Tri : recommandées d'abord
        assertThat(releases.first()["guid"]).isEqualTo("release-multi")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `the resolution constraint is lifted when the desired resolution is unknown`() {
        // Oppenheimer n'a pas de qualityProfileId : on ne connaît pas la résolution voulue → permissif
        val id = createWorkflow().getString("id")
        selectMovie(id, radarrMovieId = 2)
        selectProblem(id)

        val releases = RestAssured.given()
            .`when`().get("/api/problems/workflows/$id/releases")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")

        assertThat(releases).hasSize(1)
        assertThat(releases.first()["isRecommended"]).isEqualTo(true)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admins see the workflows of every user`() {
        insertWorkflowFor("bob")

        val list = RestAssured.given()
            .`when`().get("/api/admin/problems/workflows")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")

        assertThat(list).hasSize(1)
        assertThat(list.first()["username"]).isEqualTo("bob")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `non admins cannot access the admin problems api`() {
        RestAssured.given()
            .`when`().get("/api/admin/problems/workflows")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `an admin can take over another user's workflow without reassigning it`() {
        val bobWorkflowId = insertWorkflowFor("bob")

        // Le endpoint user ne voit pas le workflow de bob
        RestAssured.given().contentType(ContentType.JSON).body("""{"radarrMovieId": 1}""")
            .`when`().post("/api/problems/workflows/$bobWorkflowId/movie")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)

        // Le endpoint admin déroule les mêmes étapes
        val afterMovie = RestAssured.given().contentType(ContentType.JSON).body("""{"radarrMovieId": 1}""")
            .`when`().post("/api/admin/problems/workflows/$bobWorkflowId/movie")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(afterMovie.getString("currentStep")).isEqualTo("SELECT_PROBLEM")

        RestAssured.given().contentType(ContentType.JSON).body("""{"problemType": "vo_should_be_french"}""")
            .`when`().post("/api/admin/problems/workflows/$bobWorkflowId/problem")
            .then().statusCode(Response.Status.OK.statusCode)

        // Le workflow reste celui de bob
        val adminView = RestAssured.given()
            .`when`().get("/api/admin/problems/workflows/$bobWorkflowId")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(adminView.getString("username")).isEqualTo("bob")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `an admin can resolve another user's problem`() {
        val bobWorkflowId = insertWorkflowFor("bob", status = ProblemWorkflowEntity.STATUS_REPORTED)

        val workflow = RestAssured.given()
            .`when`().post("/api/admin/problems/workflows/$bobWorkflowId/resolve")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(workflow.getString("status")).isEqualTo("RESOLVED")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `an admin can delete a workflow`() {
        val bobWorkflowId = insertWorkflowFor("bob")

        RestAssured.given()
            .`when`().delete("/api/admin/problems/workflows/$bobWorkflowId")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        RestAssured.given()
            .`when`().get("/api/admin/problems/workflows/$bobWorkflowId")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)

        RestAssured.given()
            .`when`().delete("/api/admin/problems/workflows/$bobWorkflowId")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    fun `anonymous users cannot access problems`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get("/api/problems/workflows")
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `anonymous users cannot access the admin problems api`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get("/api/admin/problems/workflows")
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
