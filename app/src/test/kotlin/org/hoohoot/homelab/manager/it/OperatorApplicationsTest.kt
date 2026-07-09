package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.Test

private const val API_KEY_HEADER = "X-Api-Key"
private const val TEST_API_KEY = "test-operator-key"
private const val OPERATOR_APPLICATIONS = "/api/operator/applications"

@QuarkusTest
internal class OperatorApplicationsTest {

    @Inject
    lateinit var wireMock: WireMock

    @ConfigProperty(name = "quarkus.wiremock.devservices.port")
    var wireMockPort: Int = 0

    private val pngBytes = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
    )

    private fun logoUrl(path: String) = "http://localhost:$wireMockPort$path"

    private fun stubLogo(path: String, contentType: String = "image/png", status: Int = 200) {
        wireMock.register(
            get(urlPathEqualTo(path)).willReturn(
                aResponse().withStatus(status).withHeader("Content-Type", contentType).withBody(pngBytes)
            )
        )
    }

    private fun applicationJson(
        name: String,
        externalId: String,
        description: String = "Managed by homelab-manager-operator",
        logoUrl: String? = null,
    ) =
        """
        {
            "name": "$name",
            "category": "Médias",
            "description": "$description",
            "url": "https://${name.lowercase()}.example.org",
            "requiresVpn": false,
            "managedBy": "operator",
            "externalId": "$externalId"${logoUrl?.let { ",\n    \"logoUrl\": \"$it\"" } ?: ""}
        }
        """.trimIndent()

    private fun createApplication(name: String, externalId: String, logoUrl: String? = null): String =
        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .contentType(ContentType.JSON)
            .body(applicationJson(name, externalId, logoUrl = logoUrl))
            .`when`().post(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

    private fun listedApplication(id: String): Map<String, Any> =
        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .`when`().get(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
            .getList<Map<String, Any>>("").first { it["id"] == id }

    private fun updateApplication(id: String, body: String) {
        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .contentType(ContentType.JSON)
            .body(body)
            .`when`().put("$OPERATOR_APPLICATIONS/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)
    }

    @Test
    fun `request without api key is rejected`() {
        RestAssured.given()
            .`when`().get(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `request with wrong api key is rejected`() {
        RestAssured.given()
            .header(API_KEY_HEADER, "wrong-key")
            .`when`().get(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)

        RestAssured.given()
            .header(API_KEY_HEADER, "wrong-key")
            .contentType(ContentType.JSON)
            .body(applicationJson("Nope", "default/nope"))
            .`when`().post(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `operator can create a managed application and its managed fields round-trip`() {
        val id = createApplication("Jellyseerr", "default/jellyseerr")

        val applications = RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .`when`().get(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val created = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(created["name"]).isEqualTo("Jellyseerr")
        assertThat(created["managedBy"]).isEqualTo("operator")
        assertThat(created["externalId"]).isEqualTo("default/jellyseerr")
    }

    @Test
    fun `operator can update a drifted application`() {
        val id = createApplication("Sonarr", "default/sonarr")

        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .contentType(ContentType.JSON)
            .body(applicationJson("Sonarr Séries", "default/sonarr", description = "Séries"))
            .`when`().put("$OPERATOR_APPLICATIONS/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)

        val applications = RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .`when`().get(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val updated = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(updated["name"]).isEqualTo("Sonarr Séries")
        assertThat(updated["description"]).isEqualTo("Séries")
        assertThat(updated["updatedAt"]).isNotNull()
    }

    @Test
    fun `updating an unknown application returns 404`() {
        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .contentType(ContentType.JSON)
            .body(applicationJson("Ghost", "default/ghost"))
            .`when`().put("$OPERATOR_APPLICATIONS/{id}", "00000000-0000-0000-0000-000000000000")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    fun `operator can delete a managed application`() {
        val id = createApplication("Uptime", "default/uptime")

        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .`when`().delete("$OPERATOR_APPLICATIONS/{id}", id)
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .`when`().delete("$OPERATOR_APPLICATIONS/{id}", id)
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    fun `operator cannot create an application with a blank name`() {
        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .contentType(ContentType.JSON)
            .body(applicationJson("   ", "default/blank"))
            .`when`().post(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `application created by the operator is visible on the portal endpoint`() {
        val id = createApplication("Grafana", "monitoring/grafana")

        val applications = RestAssured.given()
            .`when`().get("/api/applications")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val created = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(created["name"]).isEqualTo("Grafana")
        assertThat(created["managedBy"]).isEqualTo("operator")
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `operator can create an application with a logo downloaded from the declared url`() {
        stubLogo("/logos/jellyfin.png")
        val url = logoUrl("/logos/jellyfin.png")

        val id = createApplication("Jellyfin-logo", "media/jellyfin-logo", logoUrl = url)

        val created = listedApplication(id)
        assertThat(created["hasLogo"]).isEqualTo(true)
        assertThat(created["logoSourceUrl"]).isEqualTo(url)

        val logoResponse = RestAssured.given()
            .`when`().get("/api/applications/{id}/logo", id)
            .then().statusCode(Response.Status.OK.statusCode)
            .contentType("image/png")
            .extract().body().asByteArray()
        assertThat(logoResponse).isEqualTo(pngBytes)
    }

    @Test
    fun `application is created without logo when the download fails`() {
        stubLogo("/logos/broken.png", status = 404)

        val id = createApplication("Broken", "media/broken", logoUrl = logoUrl("/logos/broken.png"))

        val created = listedApplication(id)
        assertThat(created["hasLogo"]).isEqualTo(false)
        assertThat(created["logoSourceUrl"]).isNull()
    }

    @Test
    fun `a logo with a forbidden content type is not stored`() {
        stubLogo("/logos/evil.html", contentType = "text/html")

        val id = createApplication("Evil", "media/evil", logoUrl = logoUrl("/logos/evil.html"))

        val created = listedApplication(id)
        assertThat(created["hasLogo"]).isEqualTo(false)
        assertThat(created["logoSourceUrl"]).isNull()
    }

    @Test
    fun `changing the declared logo url re-downloads the logo`() {
        stubLogo("/logos/radarr-v1.png")
        stubLogo("/logos/radarr-v2.png")

        val id = createApplication("Radarr", "media/radarr", logoUrl = logoUrl("/logos/radarr-v1.png"))
        updateApplication(id, applicationJson("Radarr", "media/radarr", logoUrl = logoUrl("/logos/radarr-v2.png")))

        val updated = listedApplication(id)
        assertThat(updated["hasLogo"]).isEqualTo(true)
        assertThat(updated["logoSourceUrl"]).isEqualTo(logoUrl("/logos/radarr-v2.png"))
        wireMock.verifyThat(1, getRequestedFor(urlPathEqualTo("/logos/radarr-v2.png")))
    }

    @Test
    fun `an unchanged logo url is not re-downloaded on update`() {
        stubLogo("/logos/sonarr-stable.png")
        val url = logoUrl("/logos/sonarr-stable.png")

        val id = createApplication("Sonarr-logo", "media/sonarr-logo", logoUrl = url)
        updateApplication(id, applicationJson("Sonarr-logo", "media/sonarr-logo", description = "Séries", logoUrl = url))

        val updated = listedApplication(id)
        assertThat(updated["description"]).isEqualTo("Séries")
        assertThat(updated["hasLogo"]).isEqualTo(true)
        wireMock.verifyThat(1, getRequestedFor(urlPathEqualTo("/logos/sonarr-stable.png")))
    }

    @Test
    fun `removing the declared logo url deletes the downloaded logo`() {
        stubLogo("/logos/lidarr.png")

        val id = createApplication("Lidarr", "media/lidarr", logoUrl = logoUrl("/logos/lidarr.png"))
        updateApplication(id, applicationJson("Lidarr", "media/lidarr"))

        val updated = listedApplication(id)
        assertThat(updated["hasLogo"]).isEqualTo(false)
        assertThat(updated["logoSourceUrl"]).isNull()
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `a manually uploaded logo survives an operator update without logo url`() {
        val id = createApplication("Uptime-manual", "monitoring/uptime-manual")

        RestAssured.given()
            .multiPart("name", "Uptime-manual")
            .multiPart("category", "Monitoring")
            .multiPart("description", "Sondes")
            .multiPart("url", "https://uptime-manual.example.org")
            .multiPart("requiresVpn", "false")
            .multiPart("logo", "logo.png", pngBytes, "image/png")
            .`when`().put("/api/applications/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)

        updateApplication(id, applicationJson("Uptime-manual", "monitoring/uptime-manual"))

        val updated = listedApplication(id)
        assertThat(updated["hasLogo"]).isEqualTo(true)
        assertThat(updated["logoSourceUrl"]).isNull()
    }
}
