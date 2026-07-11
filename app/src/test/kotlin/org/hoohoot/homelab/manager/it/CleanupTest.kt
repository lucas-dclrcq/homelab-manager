package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.vertx.VertxContextSupport
import io.restassured.RestAssured
import io.restassured.path.json.JsonPath
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import org.hoohoot.homelab.manager.it.config.CleanupSeed
import org.hoohoot.homelab.manager.it.config.CleanupSeed.GB
import org.hoohoot.homelab.manager.it.config.PlaybackSessionSeed
import org.hoohoot.homelab.manager.library.infra.DiskSpaceUsage
import org.hoohoot.homelab.manager.library.infra.StatsSnapshotEntity
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.infra.PlaybackSessionEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Comparator
import java.util.UUID

@QuarkusTest
internal class CleanupTest {

    @Inject
    lateinit var wireMock: WireMock

    // Série terminée dont la saison 1 (30 Go, jamais visionnée) est candidate au nettoyage
    private val wireSeriesJson = """
        {
          "id": 201, "title": "The Wire", "year": 2002, "tvdbId": 79126, "ended": true,
          "added": "2023-06-01T00:00:00Z",
          "images": [{"coverType": "poster", "remoteUrl": "https://img/wire.jpg"}],
          "tags": [1],
          "seasons": [
            {
              "seasonNumber": 1, "monitored": true,
              "statistics": {"episodeFileCount": 10, "sizeOnDisk": 30000000000, "previousAiring": "2008-03-09T00:00:00Z"}
            }
          ]
        }
    """.trimIndent()

    @BeforeEach
    fun setUp() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CleanupCandidateEntity.deleteAll()
                    .chain { _ -> CleanupCampaignEntity.deleteAll() }
                    .chain { _ -> CleanupProtectionEntity.deleteAll() }
                    .chain { _ -> StatsSnapshotEntity.deleteAll() }
                    .chain { _ -> PlaybackSessionEntity.deleteAll() }
                    .chain { _ -> ProblemWorkflowEntity.deleteAll() }
            }
        }

        wireMock.resetMappings()
        wireMock.resetRequests()
        registerDefaultStubs()
        seedDiskSnapshot(freeBytes = 50 * GB)
    }

    private fun registerDefaultStubs() {
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(
                okJson(
                    """[
                        {
                          "id": 101, "title": "Inception", "year": 2010, "tmdbId": 27205, "imdbId": "tt1375666",
                          "hasFile": true, "sizeOnDisk": 40000000000,
                          "movieFile": {"size": 40000000000, "dateAdded": "2024-01-01T00:00:00Z"},
                          "images": [{"coverType": "poster", "remoteUrl": "https://img/inception.jpg"}],
                          "tags": [1]
                        },
                        {
                          "id": 102, "title": "Le Voyage", "year": 2021, "tmdbId": 500,
                          "hasFile": true, "sizeOnDisk": 30000000000,
                          "movieFile": {"size": 30000000000, "dateAdded": "2024-01-01T00:00:00Z"},
                          "tags": [1]
                        }
                    ]"""
                )
            )
        )
        // Un seul WireMock : ce stub sert les tags Radarr et Sonarr
        wireMock.register(
            get(urlPathEqualTo("/api/v3/tag")).willReturn(okJson("""[{"id": 1, "label": "1 - john"}]"""))
        )
        wireMock.register(get(urlPathEqualTo("/api/v3/series")).willReturn(okJson("[$wireSeriesJson]")))
        wireMock.register(get(urlPathEqualTo("/api/v3/series/201")).willReturn(okJson(wireSeriesJson)))
        wireMock.register(put(urlPathEqualTo("/api/v3/series/201")).willReturn(okJson(wireSeriesJson)))
        wireMock.register(
            get(urlPathEqualTo("/api/v3/episodefile")).withQueryParam("seriesId", equalTo("201")).willReturn(
                okJson(
                    """[
                        {"id": 301, "seriesId": 201, "seasonNumber": 1, "size": 15000000000, "dateAdded": "2024-01-01T00:00:00Z"},
                        {"id": 302, "seriesId": 201, "seasonNumber": 1, "size": 15000000000, "dateAdded": "2024-01-01T00:00:00Z"}
                    ]"""
                )
            )
        )
        wireMock.register(delete(urlPathMatching("/api/v3/movie/\\d+")).willReturn(aResponse().withStatus(200)))
        wireMock.register(delete(urlPathMatching("/api/v3/episodefile/\\d+")).willReturn(aResponse().withStatus(200)))
        wireMock.register(
            get(urlPathEqualTo("/api/v3/diskspace")).willReturn(
                okJson("""[{"path": "/data", "label": "data", "freeSpace": 50000000000, "totalSpace": 1000000000000}]""")
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/Items")).withQueryParam("includeItemTypes", equalTo("Movie")).willReturn(
                okJson(
                    """{"Items": [
                        {"Id": "jf-inception", "Name": "Inception", "ProductionYear": 2010, "Type": "Movie", "ProviderIds": {"Tmdb": "27205", "Imdb": "tt1375666"}},
                        {"Id": "jf-voyage", "Name": "Le Voyage", "ProductionYear": 2021, "Type": "Movie", "ProviderIds": {"Tmdb": "500"}}
                    ], "TotalRecordCount": 2}"""
                )
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/Items")).withQueryParam("includeItemTypes", equalTo("Series")).willReturn(
                okJson(
                    """{"Items": [
                        {"Id": "jf-wire", "Name": "The Wire", "ProductionYear": 2002, "Type": "Series", "ProviderIds": {"Tvdb": "79126"}}
                    ], "TotalRecordCount": 1}"""
                )
            )
        )
    }

    private fun seedDiskSnapshot(freeBytes: Long) {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                StatsSnapshotEntity().apply {
                    source = "radarr"
                    disks = mapOf("/data" to DiskSpaceUsage(freeBytes = freeBytes, totalBytes = 1_000 * GB))
                    collectedAt = LocalDateTime.now()
                }.persist<StatsSnapshotEntity>()
            }
        }
    }

    private fun insertActiveProblemFor(radarrMovieId: Int) {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                ProblemWorkflowEntity().apply {
                    id = UUID.randomUUID()
                    username = "alice"
                    mediaType = ProblemWorkflowEntity.MEDIA_TYPE_MOVIE
                    status = ProblemWorkflowEntity.STATUS_IN_PROGRESS
                    this.radarrMovieId = radarrMovieId
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }.persist<ProblemWorkflowEntity>()
            }
        }
    }

    private fun runJob(identity: String) {
        val run = RestAssured.given()
            .`when`().post("/api/admin/jobs/$identity/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(run.getString("status")).isEqualTo("SUCCESS")
    }

    private fun getOverview(): JsonPath =
        RestAssured.given()
            .`when`().get("/api/cleanup/campaign")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun adminCampaignDetails(campaignId: UUID): JsonPath =
        RestAssured.given()
            .`when`().get("/api/admin/cleanup/campaigns/$campaignId")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

    private fun candidatesByTitle(details: JsonPath, path: String = "candidates"): Map<Any?, Map<String, Any>> =
        details.getList<Map<String, Any>>(path).associateBy { it["title"] }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `le job cleanup-scan démarre une campagne avec les candidats triés par score décroissant`() {
        // « Le Voyage » a été vu en entier récemment : il doit scorer moins qu'« Inception » jamais visionné
        PlaybackSessionSeed.insertSession(
            userName = "cleanup-watcher",
            itemId = "jf-voyage",
            itemName = "Le Voyage",
            mediaType = MediaType.MOVIE,
            completed = true,
            startedAt = LocalDateTime.now(ZoneOffset.UTC).minusDays(2),
        )

        runJob("cleanup-scan")

        val overview = getOverview()
        assertThat(overview.getString("campaign.status")).isEqualTo("ANNOUNCED")
        assertThat(overview.getString("campaign.graceEndsAt")).isNotNull()

        val candidates = overview.getList<Map<String, Any>>("campaign.candidates")
        assertThat(candidates.map { it["title"] })
            .containsExactlyInAnyOrder("Inception", "Le Voyage", "The Wire")

        val scores = candidates.map { (it["score"] as Number).toDouble() }
        assertThat(scores).isSortedAccordingTo(Comparator.reverseOrder())

        val byTitle = candidates.associateBy { it["title"] }
        val neverWatched = (byTitle["Inception"]!!["score"] as Number).toDouble()
        val recentlyCompleted = (byTitle["Le Voyage"]!!["score"] as Number).toDouble()
        assertThat(neverWatched).isGreaterThan(recentlyCompleted)

        // Le breakdown persisté est explicable : composantes présentes et total cohérent
        assertThat(overview.getList<Any>("campaign.candidates[0].scoreBreakdown.components")).isNotEmpty()
        assertThat(overview.getDouble("campaign.candidates[0].scoreBreakdown.total")).isGreaterThan(0.0)

        val wireSeason = byTitle["The Wire"]!!
        assertThat(wireSeason["mediaKind"]).isEqualTo("SEASON")
        assertThat(wireSeason["seasonNumber"]).isEqualTo(1)
        assertThat(wireSeason["displayTitle"]).isEqualTo("The Wire — Saison 1")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `un média protégé avec un problème en cours ou téléchargé récemment n'est jamais candidat`() {
        val recentIso = Instant.now().minus(5, ChronoUnit.DAYS).toString()
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(
                okJson(
                    """[
                        {
                          "id": 101, "title": "Inception", "year": 2010, "tmdbId": 27205, "hasFile": true,
                          "sizeOnDisk": 40000000000,
                          "movieFile": {"size": 40000000000, "dateAdded": "2024-01-01T00:00:00Z"}, "tags": [1]
                        },
                        {
                          "id": 103, "title": "Amélie", "year": 2001, "tmdbId": 194, "hasFile": true,
                          "sizeOnDisk": 20000000000,
                          "movieFile": {"size": 20000000000, "dateAdded": "2024-01-01T00:00:00Z"}, "tags": []
                        },
                        {
                          "id": 104, "title": "Salt", "year": 2010, "tmdbId": 27576, "hasFile": true,
                          "sizeOnDisk": 15000000000,
                          "movieFile": {"size": 15000000000, "dateAdded": "2024-01-01T00:00:00Z"}, "tags": []
                        },
                        {
                          "id": 105, "title": "Freshly Added", "year": 2026, "tmdbId": 999, "hasFile": true,
                          "sizeOnDisk": 25000000000,
                          "movieFile": {"size": 25000000000, "dateAdded": "$recentIso"}, "tags": []
                        }
                    ]"""
                )
            )
        )
        CleanupSeed.insertProtection(CleanupProtectionEntity.KIND_MOVIE, "Amélie", radarrMovieId = 103)
        insertActiveProblemFor(radarrMovieId = 104)

        runJob("cleanup-scan")

        val overview = getOverview()
        assertThat(overview.getString("campaign.status")).isEqualTo("ANNOUNCED")
        assertThat(overview.getList<Map<String, Any>>("campaign.candidates").map { it["title"] })
            .containsExactlyInAnyOrder("Inception", "The Wire")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `le veto d'un candidat le protège durablement et ne peut pas être rejoué`() {
        val campaignId = CleanupSeed.insertCampaign()
        val candidateId = CleanupSeed.insertCandidate(campaignId, "Inception", radarrMovieId = 101)

        val candidate = RestAssured.given()
            .`when`().post("/api/cleanup/candidates/$candidateId/veto")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(candidate.getString("status")).isEqualTo("PROTECTED")
        assertThat(candidate.getString("protectedBy")).isEqualTo("bob")
        assertThat(candidate.getString("protectedVia")).isEqualTo("WEB")

        val protections = RestAssured.given()
            .`when`().get("/api/cleanup/protections")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")
        assertThat(protections).hasSize(1)
        assertThat(protections.first()["mediaKind"]).isEqualTo("MOVIE")
        assertThat(protections.first()["radarrMovieId"]).isEqualTo(101)
        assertThat(protections.first()["protectedBy"]).isEqualTo("bob")
        assertThat(protections.first()["source"]).isEqualTo("VETO")

        RestAssured.given()
            .`when`().post("/api/cleanup/candidates/$candidateId/veto")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `l'exécution supprime films et saisons via radarr et sonarr en épargnant les candidats protégés`() {
        val campaignId = CleanupSeed.insertCampaign(graceEndsAt = LocalDateTime.now().minusHours(1))
        CleanupSeed.insertCandidate(campaignId, "Inception", radarrMovieId = 101, sizeBytes = 40 * GB, score = 90.0)
        CleanupSeed.insertCandidate(campaignId, "Le Voyage", radarrMovieId = 102, sizeBytes = 30 * GB, score = 60.0)
        CleanupSeed.insertCandidate(
            campaignId, "The Wire",
            mediaKind = CleanupCandidateEntity.KIND_SEASON,
            sonarrSeriesId = 201, seasonNumber = 1, sizeBytes = 30 * GB, score = 85.0,
        )
        CleanupSeed.insertCandidate(
            campaignId, "Vetoed Movie",
            radarrMovieId = 106, sizeBytes = 10 * GB, score = 70.0,
            status = CleanupCandidateEntity.STATUS_PROTECTED,
        )

        runJob("cleanup-execute")

        wireMock.verifyThat(
            deleteRequestedFor(urlPathEqualTo("/api/v3/movie/101"))
                .withQueryParam("deleteFiles", equalTo("true"))
                .withQueryParam("addImportExclusion", equalTo("false"))
        )
        // La saison est d'abord dé-monitorée puis ses fichiers d'épisodes supprimés un à un
        wireMock.verifyThat(
            putRequestedFor(urlPathEqualTo("/api/v3/series/201"))
                .withRequestBody(matchingJsonPath("$.seasons[0].monitored", equalTo("false")))
        )
        wireMock.verifyThat(deleteRequestedFor(urlPathEqualTo("/api/v3/episodefile/301")))
        wireMock.verifyThat(deleteRequestedFor(urlPathEqualTo("/api/v3/episodefile/302")))
        wireMock.verifyThat(0, deleteRequestedFor(urlPathEqualTo("/api/v3/movie/106")))

        val details = adminCampaignDetails(campaignId)
        assertThat(details.getString("status")).isEqualTo("COMPLETED")
        assertThat(details.getLong("freedBytes")).isEqualTo(100 * GB)

        val byTitle = candidatesByTitle(details)
        assertThat(byTitle["Inception"]!!["status"]).isEqualTo("DELETED")
        assertThat(byTitle["Le Voyage"]!!["status"]).isEqualTo("DELETED")
        assertThat(byTitle["The Wire"]!!["status"]).isEqualTo("DELETED")
        assertThat(byTitle["Vetoed Movie"]!!["status"]).isEqualTo("PROTECTED")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `un film déjà absent de radarr est marqué supprimé sans octet libéré`() {
        val campaignId = CleanupSeed.insertCampaign(graceEndsAt = LocalDateTime.now().minusHours(1))
        CleanupSeed.insertCandidate(campaignId, "Ghost Movie", radarrMovieId = 107, sizeBytes = 20 * GB)
        wireMock.register(delete(urlPathEqualTo("/api/v3/movie/107")).willReturn(aResponse().withStatus(404)))

        runJob("cleanup-execute")

        val details = adminCampaignDetails(campaignId)
        assertThat(details.getString("status")).isEqualTo("COMPLETED")
        assertThat(details.getLong("freedBytes")).isEqualTo(0)

        val ghost = candidatesByTitle(details)["Ghost Movie"]!!
        assertThat(ghost["status"]).isEqualTo("DELETED")
        assertThat((ghost["freedBytes"] as Number).toLong()).isEqualTo(0)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `un échec radarr n'interrompt pas les autres suppressions et le candidat peut être rejoué`() {
        val campaignId = CleanupSeed.insertCampaign(graceEndsAt = LocalDateTime.now().minusHours(1))
        val failingId = CleanupSeed.insertCandidate(campaignId, "Inception", radarrMovieId = 101, sizeBytes = 40 * GB, score = 90.0)
        CleanupSeed.insertCandidate(campaignId, "Le Voyage", radarrMovieId = 102, sizeBytes = 30 * GB, score = 60.0)
        wireMock.register(delete(urlPathEqualTo("/api/v3/movie/101")).willReturn(aResponse().withStatus(500)))

        runJob("cleanup-execute")

        val details = adminCampaignDetails(campaignId)
        assertThat(details.getString("status")).isEqualTo("COMPLETED")
        assertThat(details.getLong("freedBytes")).isEqualTo(30 * GB)
        val byTitle = candidatesByTitle(details)
        assertThat(byTitle["Inception"]!!["status"]).isEqualTo("FAILED")
        assertThat(byTitle["Inception"]!!["failureReason"].toString()).isNotBlank()
        assertThat(byTitle["Le Voyage"]!!["status"]).isEqualTo("DELETED")

        // Radarr est réparé : le retry admin rejoue la suppression et crédite la campagne
        wireMock.register(delete(urlPathEqualTo("/api/v3/movie/101")).willReturn(aResponse().withStatus(200)))

        val retried = RestAssured.given()
            .`when`().post("/api/admin/cleanup/candidates/$failingId/retry")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(retried.getString("status")).isEqualTo("DELETED")
        assertThat(retried.getLong("freedBytes")).isEqualTo(40 * GB)
        assertThat(adminCampaignDetails(campaignId).getLong("freedBytes")).isEqualTo(70 * GB)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `rien n'est supprimé quand l'espace s'est libéré pendant la période de grâce`() {
        val campaignId = CleanupSeed.insertCampaign(graceEndsAt = LocalDateTime.now().minusHours(1))
        CleanupSeed.insertCandidate(campaignId, "Inception", radarrMovieId = 101, score = 90.0)
        CleanupSeed.insertCandidate(campaignId, "Le Voyage", radarrMovieId = 102, score = 60.0)
        // Re-check en direct : 400 Go libres, au-dessus de la cible (50 Go au départ + 250 Go à libérer)
        wireMock.register(
            get(urlPathEqualTo("/api/v3/diskspace")).willReturn(
                okJson("""[{"path": "/data", "label": "data", "freeSpace": 400000000000, "totalSpace": 1000000000000}]""")
            )
        )

        runJob("cleanup-execute")

        wireMock.verifyThat(0, deleteRequestedFor(urlPathMatching("/api/v3/movie/\\d+")))

        val details = adminCampaignDetails(campaignId)
        assertThat(details.getString("status")).isEqualTo("COMPLETED")
        assertThat(details.getLong("freedBytes")).isEqualTo(0)
        assertThat(details.getList<Map<String, Any>>("candidates").map { it["status"] })
            .containsOnly("SKIPPED")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `l'annulation d'une campagne annule aussi ses candidats en attente`() {
        val campaignId = CleanupSeed.insertCampaign()
        CleanupSeed.insertCandidate(campaignId, "Inception", radarrMovieId = 101)
        CleanupSeed.insertCandidate(campaignId, "Le Voyage", radarrMovieId = 102)

        val cancelled = RestAssured.given()
            .`when`().post("/api/admin/cleanup/campaigns/$campaignId/cancel")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(cancelled.getString("status")).isEqualTo("CANCELLED")
        assertThat(cancelled.getList<Map<String, Any>>("candidates").map { it["status"] })
            .containsOnly("CANCELLED")

        // Plus de campagne active côté user
        assertThat(getOverview().getString("campaign")).isNull()
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `un utilisateur non admin ne peut pas accéder à l'administration du nettoyage`() {
        RestAssured.given()
            .`when`().get("/api/admin/cleanup/config")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
        RestAssured.given()
            .`when`().get("/api/admin/cleanup/campaigns")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
        RestAssured.given()
            .`when`().post("/api/admin/cleanup/campaigns/scan")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
        RestAssured.given()
            .`when`().post("/api/admin/cleanup/campaigns/${UUID.randomUUID()}/cancel")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }
}
