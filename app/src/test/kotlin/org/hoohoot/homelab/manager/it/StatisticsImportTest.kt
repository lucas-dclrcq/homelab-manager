package org.hoohoot.homelab.manager.it

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.vertx.VertxContextSupport
import io.restassured.RestAssured
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hamcrest.Matchers.equalTo
import org.hoohoot.homelab.manager.it.config.PlaybackSessionSeed
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.SessionSource
import org.hoohoot.homelab.manager.statistics.infra.PlaybackSessionEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@QuarkusTest
internal class StatisticsImportTest {

    @ConfigProperty(name = "statistics.import.staging-path")
    lateinit var stagingPath: String

    @BeforeEach
    fun setUp() {
        PlaybackSessionSeed.deleteAll()
        Files.deleteIfExists(Path.of(stagingPath))
    }

    private fun stageFixture() {
        javaClass.getResourceAsStream("/jellystat-backup-sample.json")!!.use { fixture ->
            val target = Path.of(stagingPath)
            target.parent?.let(Files::createDirectories)
            Files.copy(fixture, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun runImportJob(): io.restassured.response.ValidatableResponse =
        RestAssured.given()
            .`when`().post("/api/admin/jobs/jellystat-import/run")
            .then().statusCode(Response.Status.OK.statusCode)

    private fun importedSessions(): List<PlaybackSessionEntity> =
        VertxContextSupport.subscribeAndAwait {
            Panache.withSession {
                PlaybackSessionEntity.list("source", SessionSource.IMPORT)
            }
        }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `import job maps episodes and movies, filters noise, and dedups on rerun`() {
        stageFixture()

        runImportJob().body("status", equalTo("SUCCESS"))

        val sessions = importedSessions()
        // 4 entrées dans la fixture : l'audio et la session < 120 s sont ignorées
        assertThat(sessions).hasSize(2)

        val episode = sessions.single { it.mediaType == MediaType.EPISODE }
        assertThat(episode.itemId).isEqualTo("ep-1")
        assertThat(episode.itemName).isEqualTo("Épisode 2")
        assertThat(episode.seriesId).isEqualTo("series-1")
        // Nom de série issu de jf_library_episodes, sans le suffixe [tvdbid-...]
        assertThat(episode.seriesName).isEqualTo("Funboys")
        assertThat(episode.seasonNumber).isEqualTo(1)
        assertThat(episode.episodeNumber).isEqualTo(2)
        assertThat(episode.userName).isEqualTo("oknozor")
        assertThat(episode.playDurationSeconds).isEqualTo(1300)
        assertThat(episode.runtimeSeconds).isEqualTo(1445)
        // 1300/1445 ≈ 90 % >= seuil de 85 % : complété
        assertThat(episode.completed).isTrue()
        assertThat(episode.platform).isEqualTo("WEB")
        assertThat(episode.importKey).isEqualTo("import-key-episode")
        assertThat(episode.endedAt).isEqualTo("2026-02-01T14:39:47.712")

        val movie = sessions.single { it.mediaType == MediaType.MOVIE }
        assertThat(movie.itemId).isEqualTo("movie-1")
        assertThat(movie.runtimeSeconds).isEqualTo(4752)
        // 3000/4752 ≈ 63 % : non complété
        assertThat(movie.completed).isFalse()
        assertThat(movie.progressPercent).isEqualByComparingTo(BigDecimal("63.13"))
        assertThat(movie.platform).isEqualTo("ANDROID_TV")

        // Rejouer le même backup ne crée aucun doublon (dédup sur import_key)
        stageFixture()
        runImportJob().body("status", equalTo("SUCCESS"))
        assertThat(importedSessions()).hasSize(2)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `import job fails explicitly when no backup is staged`() {
        runImportJob().body("status", equalTo("FAILURE"))
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `upload stages the backup and triggers the import in the background`() {
        val upload = File.createTempFile("jellystat-backup", ".json")
        javaClass.getResourceAsStream("/jellystat-backup-sample.json")!!.use { fixture ->
            Files.copy(fixture, upload.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        RestAssured.given()
            .multiPart("file", upload, "application/json")
            .`when`().post("/api/admin/statistics/import")
            .then().statusCode(Response.Status.ACCEPTED.statusCode)
            .body("jobIdentity", equalTo("jellystat-import"))

        // L'import tourne en arrière-plan : le fichier stagé est supprimé en fin de succès
        awaitUntil { Files.notExists(Path.of(stagingPath)) && importedSessions().size == 2 }
        assertThat(importedSessions()).hasSize(2)
    }

    private fun awaitUntil(timeoutMs: Long = 15_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(100)
        }
        error("Condition non remplie après $timeoutMs ms")
    }
}
