package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import io.quarkus.vertx.VertxContextSupport
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.jobs.JobExecutionEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FastSyncProfile : QuarkusTestProfile {
    override fun getConfigOverrides() = mapOf(
        "radarr-sync.every" to "2s",
        "stats-sync.initial-delay" to "0s",
    )
}

@QuarkusTest
@TestProfile(FastSyncProfile::class)
internal class ScheduledJobTrackingTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        wireMock.resetMappings()
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(okJson("""[{"id": 1, "title": "Dune"}]"""))
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/diskspace")).willReturn(
                okJson("""[{"path": "/data", "label": "data", "freeSpace": 500, "totalSpace": 2000}]""")
            )
        )
    }

    @Test
    fun `scheduled fires are recorded in job_execution`() {
        val deadline = System.currentTimeMillis() + 20_000
        var row: JobExecutionEntity? = null
        while (row == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(1_000)
            row = VertxContextSupport.subscribeAndAwait {
                Panache.withSession { JobExecutionEntity.findById("radarr-sync") }
            }
        }
        assertThat(row).describedAs("job_execution row for radarr-sync after scheduled fires").isNotNull
        assertThat(row!!.manual).isFalse
        assertThat(row!!.lastDurationMs).isNotNull
    }
}
