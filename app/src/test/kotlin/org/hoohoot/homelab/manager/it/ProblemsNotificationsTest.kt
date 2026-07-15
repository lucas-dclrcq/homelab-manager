package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.shared.matrix.MatrixRoomProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class ProblemsNotificationsTest {

    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    @Inject
    lateinit var wireMock: WireMock

    private lateinit var supportRoomId: String

    @BeforeEach
    fun setUp() {
        supportRoomId = synapseTestClient.createRoom("support-${System.nanoTime()}")
        roomProvider.support = supportRoomId

        wireMock.resetMappings()
        wireMock.resetRequests()

        // Stub Radarr movies
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(
                okJson(
                    """[
                        {
                          "id": 1, "title": "Dune: Part Two", "year": 2024, "hasFile": true,
                          "overview": "Paul Atreides leads the revolt.", "qualityProfileId": 4,
                          "images": [{"coverType": "poster", "remoteUrl": "https://img/dune2.jpg"}],
                          "movieFile": {
                            "quality": {"quality": {"name": "Bluray-720p"}},
                            "languages": [{"id": 1, "name": "English"}]
                          }
                        },
                        {"id": 2, "title": "Oppenheimer", "year": 2023}
                    ]"""
                )
            )
        )

        // Stub quality profiles
        wireMock.register(
            get(urlPathEqualTo("/api/v3/qualityprofile")).willReturn(
                okJson(
                    """[
                        {
                          "id": 4, "name": "HD-1080p", "cutoff": 1003,
                          "items": [
                            {"quality": {"id": 3, "name": "WEBDL-1080p", "resolution": 1080}, "allowed": true},
                            {"quality": {"id": 7, "name": "Bluray-1080p", "resolution": 1080}, "allowed": true}
                          ]
                        }
                    ]"""
                )
            )
        )

        // Stub release grab
        wireMock.register(
            post(urlPathEqualTo("/api/v3/release")).willReturn(okJson("""{"guid": "release-1", "indexerId": 1}"""))
        )

        // Stub Sonarr series
        wireMock.register(
            get(urlPathEqualTo("/api/v3/series")).willReturn(
                okJson(
                    """[
                        {"id": 10, "title": "Severance", "year": 2022, "overview": "Work-life balance gone wrong."}
                    ]"""
                )
            )
        )
    }

    private fun createWorkflow(mediaType: String = "movie"): String =
        RestAssured.given().contentType(ContentType.JSON).body("""{"mediaType": "$mediaType"}""")
            .`when`().post("/api/problems/workflows")
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

    private fun selectMovie(id: String, radarrMovieId: Int = 1) {
        RestAssured.given().contentType(ContentType.JSON).body("""{"radarrMovieId": $radarrMovieId}""")
            .`when`().post("/api/problems/workflows/$id/movie")
            .then().statusCode(Response.Status.OK.statusCode)
    }

    private fun selectProblem(id: String, problemType: String = "other", description: String = "Test") {
        RestAssured.given().contentType(ContentType.JSON)
            .body("""{"problemType": "$problemType", "description": "$description"}""")
            .`when`().post("/api/problems/workflows/$id/problem")
            .then().statusCode(Response.Status.OK.statusCode)
    }

    private fun resolveWorkflow(id: String) {
        RestAssured.given()
            .`when`().post("/api/problems/workflows/$id/resolve")
            .then().statusCode(Response.Status.OK.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `la creation d'un workflow envoie une notification Matrix`() {
        createWorkflow()

        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).contains("🔧 Nouveau problème")
        assertThat(lastMessage.get("body").asText()).contains("@alice")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `le signalement d'un probleme envoie une notification en thread du message de creation`() {
        val id = createWorkflow()

        // Récupère l'event_id du message de création (racine du thread)
        val createEventId = synapseTestClient.getLastMessageEvent(supportRoomId).get("event_id").asText()

        selectMovie(id)
        selectProblem(id, problemType = "other", description = "Le fichier est corrompu")

        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).contains("🐛 Problème signalé")
        assertThat(lastMessage.get("body").asText()).contains("@alice")
        assertThat(lastMessage.get("body").asText()).contains("Le fichier est corrompu")

        // Vérifie que le message est en thread du message de création
        val lastEvent = synapseTestClient.getLastMessageEvent(supportRoomId)
        val relatesTo = lastEvent.get("content").get("m.relates_to")
        assertThat(relatesTo).isNotNull()
        assertThat(relatesTo.get("rel_type").asText()).isEqualTo("m.thread")
        assertThat(relatesTo.get("event_id").asText()).isEqualTo(createEventId)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `l auto-resolution envoie un message condescendant et une reaction sur le message racine`() {
        val id = createWorkflow()
        selectMovie(id)
        selectProblem(id, problemType = "other", description = "Mauvais ratio")

        resolveWorkflow(id)

        // Vérifie le message de résolution
        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).contains("🙄 Problème résolu")
        assertThat(lastMessage.get("body").asText()).contains("@alice")
        assertThat(lastMessage.get("body").asText()).contains("fallait juste chercher un peu")

        // Vérifie que le message est en thread
        val lastEvent = synapseTestClient.getLastMessageEvent(supportRoomId)
        val resolutionRelatesTo = lastEvent.get("content").get("m.relates_to")
        assertThat(resolutionRelatesTo.get("rel_type").asText()).isEqualTo("m.thread")

        // Vérifie la réaction ✅ sur le message racine du thread
        val reactions = synapseTestClient.getReactions(supportRoomId)
        assertThat(reactions).isNotEmpty()

        val lastReaction = reactions.first()
        val reactionKey = if (lastReaction.has("key")) lastReaction.get("key").asText()
        else lastReaction.get("content").get("m.relates_to").get("key").asText()
        assertThat(reactionKey).isEqualTo("✅")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `la resolution par un admin envoie un message normal`() {
        val id = createWorkflow()
        selectMovie(id)
        selectProblem(id, problemType = "other", description = "Problème test")

        // Résolution par admin via l'endpoint admin
        RestAssured.given()
            .`when`().post("/api/admin/problems/workflows/$id/resolve")
            .then().statusCode(Response.Status.OK.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(supportRoomId)
        assertThat(lastMessage.get("body").asText()).contains("✅ Problème résolu")
        assertThat(lastMessage.get("body").asText()).contains("@alice")
        assertThat(lastMessage.get("body").asText()).contains("@admin")
    }
}
