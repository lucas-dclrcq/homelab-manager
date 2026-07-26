package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.notifications.api.NanaResource
import org.hoohoot.homelab.manager.shared.matrix.MatrixRoomProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(NanaResource::class)
internal class BookNotificationsTest {
    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var booksRoomId: String

    @BeforeEach
    fun setUp() {
        booksRoomId = synapseTestClient.createRoom("books-${System.nanoTime()}")
        roomProvider.books = booksRoomId
    }

    @Test
    fun `should send book downloaded notification on success`() {
        val payload = """
            {
                "event": "download.succeeded",
                "download": {
                    "id": 1,
                    "md5": "abc123",
                    "title": "Dune",
                    "extension": "epub",
                    "requestedBy": "alice",
                    "status": "SUCCESS",
                    "filePath": "/books/Dune.epub",
                    "sizeBytes": 5242880,
                    "errorMessage": null,
                    "requestedAt": "2026-07-25T10:00:00Z",
                    "finishedAt": "2026-07-25T10:01:00Z"
                }
            }
        """.trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseTestClient.getLastMessage(booksRoomId)
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "📚 Ebook téléchargé\nDune (epub)\n👤 Demandé par : alice\n💾 Taille : 5.0 MB",
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>📚 Ebook téléchargé</h1><p>Dune (epub)<br>👤 Demandé par : alice<br>💾 Taille : 5.0 MB</p>",
        )
    }

    @Test
    fun `should send failure notification with error message`() {
        val payload = """
            {
                "event": "download.failed",
                "download": {
                    "title": "Neuromancer",
                    "requestedBy": "bob",
                    "status": "FAILED",
                    "errorMessage": "mirror unreachable"
                }
            }
        """.trimIndent()

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(booksRoomId).get("body").asText()
        assertThat(body).contains("❌ Échec du téléchargement")
        assertThat(body).contains("Neuromancer")
        assertThat(body).contains("👤 Demandé par : bob")
        assertThat(body).contains("⚠️ mirror unreachable")
    }

    @Test
    fun `should handle minimal payload with unknown values`() {
        val payload = """{"event": "download.succeeded"}"""

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val body = synapseTestClient.getLastMessage(booksRoomId).get("body").asText()
        assertThat(body).contains("unknown (unknown)")
        assertThat(body).contains("👤 Demandé par : unknown")
        assertThat(body).contains("💾 Taille : unknown")
    }

    @Test
    fun `should ignore unknown event type`() {
        val payload = """{"event": "download.started", "download": {"title": "Foundation"}}"""

        RestAssured.given().contentType(ContentType.JSON).body(payload)
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val messageCount = synapseTestClient.getMessageCount(booksRoomId)
        assertThat(messageCount).isEqualTo(0)
    }
}
