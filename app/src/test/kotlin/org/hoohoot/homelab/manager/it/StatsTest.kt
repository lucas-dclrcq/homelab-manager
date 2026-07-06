package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.portal.resource.StatsResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(StatsResource::class)
internal class StatsTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        wireMock.resetMappings()
        wireMock.register(
            get(urlPathEqualTo("/api/v3/movie")).willReturn(
                okJson("""[{"id": 1, "title": "Dune"}, {"id": 2, "title": "Oppenheimer"}, {"id": 3, "title": "The Substance"}]""")
            )
        )
        wireMock.register(
            get(urlPathEqualTo("/api/v3/series")).willReturn(
                okJson(
                    """[
                        {"id": 10, "title": "The Bear", "statistics": {"episodeFileCount": 28}},
                        {"id": 11, "title": "Severance", "statistics": {"episodeFileCount": 19}}
                    ]"""
                )
            )
        )
        // Radarr and Sonarr share the same WireMock: both clients see the same mounts,
        // which the dedupe-by-path aggregation must collapse to a single disk
        wireMock.register(
            get(urlPathEqualTo("/api/v3/diskspace")).willReturn(
                okJson("""[{"path": "/data", "label": "data", "freeSpace": 500, "totalSpace": 2000}]""")
            )
        )
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `should aggregate library and disk stats from radarr and sonarr`() {
        val stats = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(stats.getInt("movieCount")).isEqualTo(3)
        assertThat(stats.getInt("seriesCount")).isEqualTo(2)
        assertThat(stats.getInt("episodeCount")).isEqualTo(47)
        assertThat(stats.getLong("diskTotalBytes")).isEqualTo(2000)
        assertThat(stats.getLong("diskFreeBytes")).isEqualTo(500)
        assertThat(stats.getLong("diskUsedBytes")).isEqualTo(1500)
    }

    @Test
    fun `anonymous user cannot read stats`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get()
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
