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
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import org.hoohoot.homelab.manager.it.config.CleanupSeed
import org.hoohoot.homelab.manager.it.config.CleanupSeed.GB
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.shared.matrix.MatrixRoomProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@QuarkusTest
internal class CleanupSuggestionsTest {

    @Inject
    lateinit var wireMock: WireMock

    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var mediaRoomId: String

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
            },
            {
              "seasonNumber": 2, "monitored": true,
              "statistics": {"episodeFileCount": 8, "sizeOnDisk": 20000000000, "previousAiring": "2009-03-09T00:00:00Z"}
            }
          ]
        }
    """.trimIndent()

    @BeforeEach
    fun setUp() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CleanupSuggestionEntity.deleteAll()
                    .chain { _ -> CleanupProtectionEntity.deleteAll() }
            }
        }

        wireMock.resetMappings()
        wireMock.resetRequests()
        registerDefaultStubs()

        mediaRoomId = synapseTestClient.createRoom("media-${System.nanoTime()}")
        roomProvider.media = mediaRoomId
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
                        {"id": 302, "seriesId": 201, "seasonNumber": 1, "size": 15000000000, "dateAdded": "2024-01-01T00:00:00Z"},
                        {"id": 303, "seriesId": 201, "seasonNumber": 2, "size": 20000000000, "dateAdded": "2024-01-01T00:00:00Z"}
                    ]"""
                )
            )
        )
        wireMock.register(delete(urlPathMatching("/api/v3/movie/\\d+")).willReturn(aResponse().withStatus(200)))
        wireMock.register(delete(urlPathMatching("/api/v3/series/\\d+")).willReturn(aResponse().withStatus(200)))
        wireMock.register(delete(urlPathMatching("/api/v3/episodefile/\\d+")).willReturn(aResponse().withStatus(200)))
    }

    private fun suggest(mediaKind: String, radarrMovieId: Int? = null, sonarrSeriesId: Int? = null, seasonNumber: Int? = null): JsonPath =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "mediaKind" to mediaKind,
                    "radarrMovieId" to radarrMovieId,
                    "sonarrSeriesId" to sonarrSeriesId,
                    "seasonNumber" to seasonNumber,
                )
            )
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath()

    private fun runJob(identity: String) {
        val run = RestAssured.given()
            .`when`().post("/api/admin/jobs/$identity/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(run.getString("status")).isEqualTo("SUCCESS")
    }

    private fun listSuggestions(): List<Map<String, Any>> =
        RestAssured.given()
            .`when`().get("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList("")

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `la suggestion d'un film est annoncée sur matrix et visible dans les suggestions en attente`() {
        val suggestion = suggest("MOVIE", radarrMovieId = 101)

        assertThat(suggestion.getString("status")).isEqualTo("PENDING")
        assertThat(suggestion.getString("suggestedBy")).isEqualTo("bob")
        assertThat(suggestion.getString("title")).isEqualTo("Inception")
        assertThat(suggestion.getLong("sizeBytes")).isEqualTo(40 * GB)
        assertThat(suggestion.getString("deleteAfter")).isNotBlank()

        val announcement = synapseTestClient.getLastMessageEvent(mediaRoomId)
        assertThat(announcement.get("content").get("body").asText())
            .contains("bob propose de supprimer")
            .contains("Inception")
            .contains("❌")

        val stored = CleanupSeed.suggestion(UUID.fromString(suggestion.getString("id")))
        assertThat(stored?.announcementEventId).isEqualTo(announcement.get("event_id").asText())

        val pending = listSuggestions()
        assertThat(pending).hasSize(1)
        assertThat(pending.first()["title"]).isEqualTo("Inception")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `une suggestion dupliquée, protégée ou introuvable est rejetée`() {
        suggest("MOVIE", radarrMovieId = 101)
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "MOVIE", "radarrMovieId" to 101))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CONFLICT.statusCode)

        CleanupSeed.insertProtection(CleanupProtectionEntity.KIND_MOVIE, "Le Voyage", radarrMovieId = 102)
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "MOVIE", "radarrMovieId" to 102))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CONFLICT.statusCode)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "MOVIE", "radarrMovieId" to 999))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `sans veto le film est supprimé via radarr à l'échéance et l'issue est annoncée`() {
        val suggestionId = UUID.fromString(suggest("MOVIE", radarrMovieId = 101).getString("id"))

        val announcementEventId = synapseTestClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()
        synapseTestClient.sendReaction(mediaRoomId, announcementEventId, "👍")

        CleanupSeed.makeSuggestionDue(suggestionId)
        runJob("cleanup-suggestions")

        wireMock.verifyThat(
            deleteRequestedFor(urlPathEqualTo("/api/v3/movie/101"))
                .withQueryParam("deleteFiles", equalTo("true"))
                .withQueryParam("addImportExclusion", equalTo("false"))
        )

        val executed = CleanupSeed.suggestion(suggestionId)
        assertThat(executed?.status).isEqualTo(CleanupSuggestionEntity.STATUS_DELETED)
        assertThat(executed?.freedBytes).isEqualTo(40 * GB)

        assertThat(synapseTestClient.getLastMessage(mediaRoomId).get("body").asText())
            .contains("Suppression effectuée")
            .contains("Inception")

        val recent = listSuggestions()
        assertThat(recent).hasSize(1)
        assertThat(recent.first()["status"]).isEqualTo("DELETED")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `une réaction ❌ sur l'annonce vaut veto et rien n'est supprimé`() {
        val suggestionId = UUID.fromString(suggest("MOVIE", radarrMovieId = 101).getString("id"))

        val announcementEventId = synapseTestClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()
        synapseTestClient.sendReaction(mediaRoomId, announcementEventId, "❌")

        CleanupSeed.makeSuggestionDue(suggestionId)
        runJob("cleanup-suggestions")

        wireMock.verifyThat(0, deleteRequestedFor(urlPathMatching("/api/v3/movie/\\d+")))

        val vetoed = CleanupSeed.suggestion(suggestionId)
        assertThat(vetoed?.status).isEqualTo(CleanupSuggestionEntity.STATUS_VETOED)
        assertThat(vetoed?.vetoedBy).isEqualTo("admin")

        assertThat(synapseTestClient.getLastMessage(mediaRoomId).get("body").asText())
            .contains("Veto")
            .contains("Inception")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `une réaction ❌ est acquittée immédiatement sans attendre l'échéance`() {
        synapseTestClient.inviteUser(mediaRoomId, "@johnnybot:localhost")

        val suggestionId = UUID.fromString(suggest("MOVIE", radarrMovieId = 101).getString("id"))
        val announcementEventId = synapseTestClient.getLastMessageEvent(mediaRoomId).get("event_id").asText()

        synapseTestClient.sendReaction(mediaRoomId, announcementEventId, "❌")

        await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofMillis(500)).untilAsserted {
            assertThat(CleanupSeed.suggestion(suggestionId)?.status)
                .isEqualTo(CleanupSuggestionEntity.STATUS_VETOED)
        }

        val vetoed = CleanupSeed.suggestion(suggestionId)
        assertThat(vetoed?.vetoedBy).isEqualTo("admin")

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted {
            assertThat(synapseTestClient.getLastMessage(mediaRoomId).get("body").asText())
                .contains("Veto")
                .contains("Inception")
        }
        wireMock.verifyThat(0, deleteRequestedFor(urlPathMatching("/api/v3/movie/\\d+")))

        val recent = listSuggestions()
        assertThat(recent).hasSize(1)
        assertThat(recent.first()["status"]).isEqualTo("VETOED")
        assertThat(recent.first()["vetoedBy"]).isEqualTo("admin")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `la suggestion d'une série entière cumule ses saisons et la supprime via sonarr à l'échéance`() {
        val suggestion = suggest("SERIES", sonarrSeriesId = 201)
        assertThat(suggestion.getLong("sizeBytes")).isEqualTo(50 * GB)

        val suggestionId = UUID.fromString(suggestion.getString("id"))
        CleanupSeed.makeSuggestionDue(suggestionId)
        runJob("cleanup-suggestions")

        wireMock.verifyThat(
            deleteRequestedFor(urlPathEqualTo("/api/v3/series/201"))
                .withQueryParam("deleteFiles", equalTo("true"))
                .withQueryParam("addImportListExclusion", equalTo("false"))
        )
        val deleted = CleanupSeed.suggestion(suggestionId)
        assertThat(deleted?.status).isEqualTo(CleanupSuggestionEntity.STATUS_DELETED)
        assertThat(deleted?.freedBytes).isEqualTo(50 * GB)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `la suggestion d'une saison précise ne supprime que les fichiers de cette saison`() {
        val suggestion = suggest("SEASON", sonarrSeriesId = 201, seasonNumber = 1)
        assertThat(suggestion.getLong("sizeBytes")).isEqualTo(30 * GB)
        assertThat(suggestion.getString("displayTitle")).isEqualTo("The Wire — Saison 1")

        val suggestionId = UUID.fromString(suggestion.getString("id"))
        CleanupSeed.makeSuggestionDue(suggestionId)
        runJob("cleanup-suggestions")

        wireMock.verifyThat(
            putRequestedFor(urlPathEqualTo("/api/v3/series/201"))
                .withRequestBody(matchingJsonPath("$.seasons[0].monitored", equalTo("false")))
        )
        wireMock.verifyThat(deleteRequestedFor(urlPathEqualTo("/api/v3/episodefile/301")))
        wireMock.verifyThat(deleteRequestedFor(urlPathEqualTo("/api/v3/episodefile/302")))
        wireMock.verifyThat(0, deleteRequestedFor(urlPathEqualTo("/api/v3/episodefile/303")))
        wireMock.verifyThat(0, deleteRequestedFor(urlPathMatching("/api/v3/series/\\d+")))

        val deleted = CleanupSeed.suggestion(suggestionId)
        assertThat(deleted?.status).isEqualTo(CleanupSuggestionEntity.STATUS_DELETED)
        assertThat(deleted?.freedBytes).isEqualTo(30 * GB)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `la protection d'une seule saison bloque la suggestion de la série entière`() {
        CleanupSeed.insertProtection(
            CleanupProtectionEntity.KIND_SEASON, "The Wire",
            sonarrSeriesId = 201, seasonNumber = 2,
        )

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "SERIES", "sonarrSeriesId" to 201))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CONFLICT.statusCode)

        suggest("SEASON", sonarrSeriesId = 201, seasonNumber = 1)
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "SEASON", "sonarrSeriesId" to 201, "seasonNumber" to 2))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `une suggestion de saison et une suggestion de la même série entière s'excluent mutuellement`() {
        suggest("SEASON", sonarrSeriesId = 201, seasonNumber = 1)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "SERIES", "sonarrSeriesId" to 201))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CONFLICT.statusCode)

        CleanupSeed.deleteAll()
        suggest("SERIES", sonarrSeriesId = 201)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "SEASON", "sonarrSeriesId" to 201, "seasonNumber" to 2))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `une suggestion de saison sans numéro de saison est invalide`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("mediaKind" to "SEASON", "sonarrSeriesId" to 201))
            .`when`().post("/api/cleanup/suggestions")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `une saison protégée pendant la période de grâce empêche la suppression de la série à l'échéance`() {
        val announcementEventId = synapseTestClient.sendMessage(mediaRoomId, "annonce de test")
        val suggestionId = CleanupSeed.insertSuggestion(
            "The Wire",
            mediaKind = CleanupSuggestionEntity.KIND_SERIES,
            sonarrSeriesId = 201,
            sizeBytes = 50 * GB,
            announcementEventId = announcementEventId,
            deleteAfter = LocalDateTime.now().minusHours(1),
        )
        CleanupSeed.insertProtection(
            CleanupProtectionEntity.KIND_SEASON, "The Wire",
            sonarrSeriesId = 201, seasonNumber = 2,
        )

        runJob("cleanup-suggestions")

        wireMock.verifyThat(0, deleteRequestedFor(urlPathMatching("/api/v3/series/\\d+")))
        val skipped = CleanupSeed.suggestion(suggestionId)
        assertThat(skipped?.status).isEqualTo(CleanupSuggestionEntity.STATUS_SKIPPED)
        assertThat(skipped?.failureReason).contains("protégé")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `une suggestion jamais annoncée sur matrix n'est pas supprimée par prudence`() {
        val suggestionId = CleanupSeed.insertSuggestion(
            "Inception",
            radarrMovieId = 101,
            announcementEventId = null,
            deleteAfter = LocalDateTime.now().minusHours(1),
        )

        runJob("cleanup-suggestions")

        wireMock.verifyThat(0, deleteRequestedFor(urlPathMatching("/api/v3/movie/\\d+")))
        val skipped = CleanupSeed.suggestion(suggestionId)
        assertThat(skipped?.status).isEqualTo(CleanupSuggestionEntity.STATUS_SKIPPED)
        assertThat(skipped?.failureReason).contains("annonce")
    }
}
