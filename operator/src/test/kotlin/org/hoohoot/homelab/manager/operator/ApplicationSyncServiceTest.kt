package org.hoohoot.homelab.manager.operator

import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@QuarkusTestResource(WireMockTestResource::class)
internal class ApplicationSyncServiceTest {

    @Inject
    lateinit var syncService: ApplicationSyncService

    private val server get() = WireMockTestResource.server

    private val managedJellyfin = """
        {
            "id": "11111111-1111-1111-1111-111111111111",
            "name": "jellyfin",
            "category": "Uncategorized",
            "description": "Managed by homelab-manager-operator",
            "url": "https://jellyfin.example.org",
            "requiresVpn": false,
            "hasLogo": false,
            "managedBy": "operator",
            "externalId": "media/jellyfin",
            "updatedAt": null
        }
    """.trimIndent()

    private val manualGrafana = """
        {
            "id": "22222222-2222-2222-2222-222222222222",
            "name": "Grafana",
            "category": "Monitoring",
            "description": "Dashboards",
            "url": "https://grafana.example.org",
            "requiresVpn": true,
            "hasLogo": true,
            "managedBy": null,
            "externalId": null,
            "updatedAt": null
        }
    """.trimIndent()

    private val desiredJellyfin = DesiredApplication(
        externalId = "media/jellyfin",
        name = "jellyfin",
        category = "Uncategorized",
        description = DEFAULT_DESCRIPTION,
        url = "https://jellyfin.example.org",
        requiresVpn = false,
    )

    @BeforeEach
    fun resetStubs() {
        server.resetAll()
        WireMockTestResource.stubTokenEndpoint()
        server.stubFor(post(urlEqualTo("/api/applications")).willReturn(aResponse().withStatus(201)))
        server.stubFor(put(anyUrl()).willReturn(aResponse().withStatus(200)))
        server.stubFor(com.github.tomakehurst.wiremock.client.WireMock.delete(anyUrl()).willReturn(aResponse().withStatus(204)))
    }

    private fun stubList(vararg applications: String) {
        server.stubFor(
            get(urlEqualTo("/api/applications")).willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(applications.joinToString(",", prefix = "[", postfix = "]"))
            )
        )
    }

    @Test
    fun `creates a missing application with managed fields and bearer token`() {
        stubList()

        syncService.syncAll(listOf(desiredJellyfin))

        server.verify(
            postRequestedFor(urlEqualTo("/api/applications"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .withRequestBodyPart(aMultipart().withName("name").withBody(equalTo("jellyfin")).build())
                .withRequestBodyPart(aMultipart().withName("category").withBody(equalTo("Uncategorized")).build())
                .withRequestBodyPart(aMultipart().withName("url").withBody(equalTo("https://jellyfin.example.org")).build())
                .withRequestBodyPart(aMultipart().withName("requiresVpn").withBody(equalTo("false")).build())
                .withRequestBodyPart(aMultipart().withName("managedBy").withBody(equalTo("operator")).build())
                .withRequestBodyPart(aMultipart().withName("externalId").withBody(equalTo("media/jellyfin")).build())
        )
    }

    @Test
    fun `updates a drifted managed application`() {
        stubList(managedJellyfin)

        syncService.syncAll(listOf(desiredJellyfin.copy(requiresVpn = true, category = "Médias")))

        server.verify(
            putRequestedFor(urlEqualTo("/api/applications/11111111-1111-1111-1111-111111111111"))
                .withRequestBodyPart(aMultipart().withName("category").withBody(equalTo("Médias")).build())
                .withRequestBodyPart(aMultipart().withName("requiresVpn").withBody(equalTo("true")).build())
        )
    }

    @Test
    fun `does nothing when the managed application is up to date`() {
        stubList(managedJellyfin)

        syncService.syncAll(listOf(desiredJellyfin))

        server.verify(0, postRequestedFor(urlEqualTo("/api/applications")))
        server.verify(0, putRequestedFor(urlEqualTo("/api/applications/11111111-1111-1111-1111-111111111111")))
        server.verify(0, deleteRequestedFor(urlEqualTo("/api/applications/11111111-1111-1111-1111-111111111111")))
    }

    @Test
    fun `deletes orphaned managed applications but never manual ones`() {
        stubList(managedJellyfin, manualGrafana)

        syncService.syncAll(emptyList())

        server.verify(deleteRequestedFor(urlEqualTo("/api/applications/11111111-1111-1111-1111-111111111111")))
        server.verify(0, deleteRequestedFor(urlEqualTo("/api/applications/22222222-2222-2222-2222-222222222222")))
    }

    @Test
    fun `deleteIfManaged ignores routes not backed by a managed application`() {
        stubList(manualGrafana)

        syncService.deleteIfManaged("monitoring/grafana")

        server.verify(0, deleteRequestedFor(urlEqualTo("/api/applications/22222222-2222-2222-2222-222222222222")))
    }

    @Test
    fun `upsert creates the application for a single route`() {
        stubList()

        syncService.upsert(desiredJellyfin)

        server.verify(postRequestedFor(urlEqualTo("/api/applications")))
    }
}
