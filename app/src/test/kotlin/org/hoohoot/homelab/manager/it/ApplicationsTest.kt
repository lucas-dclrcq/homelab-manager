package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.specification.MultiPartSpecification
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.portal.resource.ApplicationsResource
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(ApplicationsResource::class)
internal class ApplicationsTest {

    private val pngBytes = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
    )

    private fun utf8Part(name: String, value: String): MultiPartSpecification =
        MultiPartSpecBuilder(value).controlName(name).charset("UTF-8").build()

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin can create an application with a logo and retrieve it`() {
        val id = RestAssured.given()
            .multiPart("name", "Jellyfin")
            .multiPart(utf8Part("category", "Médias"))
            .multiPart("description", "Serveur de streaming du homelab")
            .multiPart("url", "https://jellyfin.example.org")
            .multiPart("requiresVpn", "true")
            .multiPart("logo", "logo.png", pngBytes, "image/png")
            .`when`().post()
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

        val applications = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val created = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(created["name"]).isEqualTo("Jellyfin")
        assertThat(created["category"]).isEqualTo("Médias")
        assertThat(created["requiresVpn"]).isEqualTo(true)
        assertThat(created["hasLogo"]).isEqualTo(true)

        val logoResponse = RestAssured.given()
            .`when`().get("/{id}/logo", id)
            .then().statusCode(Response.Status.OK.statusCode)
            .contentType("image/png")
            .extract().asByteArray()
        assertThat(logoResponse).isEqualTo(pngBytes)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `application without logo has no logo endpoint result`() {
        val id = RestAssured.given()
            .multiPart("name", "Grafana")
            .multiPart("category", "Monitoring")
            .multiPart("description", "Dashboards")
            .multiPart("url", "https://grafana.example.org")
            .multiPart("requiresVpn", "false")
            .`when`().post()
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

        RestAssured.given()
            .`when`().get("/{id}/logo", id)
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin cannot create an application with missing fields`() {
        RestAssured.given()
            .multiPart("name", "Incomplete")
            .`when`().post()
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin cannot upload a logo with a forbidden content type`() {
        RestAssured.given()
            .multiPart("name", "Evil")
            .multiPart("category", "Médias")
            .multiPart("description", "desc")
            .multiPart("url", "https://evil.example.org")
            .multiPart("requiresVpn", "false")
            .multiPart("logo", "logo.html", "<script></script>".toByteArray(), "text/html")
            .`when`().post()
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    private fun createApplication(name: String, withLogo: Boolean = false): String {
        val request = RestAssured.given()
            .multiPart("name", name)
            .multiPart(utf8Part("category", "Médias"))
            .multiPart("description", "desc")
            .multiPart("url", "https://$name.example.org".lowercase())
            .multiPart("requiresVpn", "false")
        if (withLogo) {
            request.multiPart("logo", "logo.png", pngBytes, "image/png")
        }
        return request
            .`when`().post()
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin can update an application with a new logo`() {
        val id = createApplication("Immich", withLogo = false)

        val newLogo = pngBytes + byteArrayOf(0x01, 0x02)
        RestAssured.given()
            .multiPart("name", "Immich Photos")
            .multiPart(utf8Part("category", "Photos"))
            .multiPart("description", "Sauvegarde de photos")
            .multiPart("url", "https://photos.example.org")
            .multiPart("requiresVpn", "true")
            .multiPart("logo", "logo.png", newLogo, "image/png")
            .`when`().put("/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)

        val applications = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val updated = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(updated["name"]).isEqualTo("Immich Photos")
        assertThat(updated["category"]).isEqualTo("Photos")
        assertThat(updated["requiresVpn"]).isEqualTo(true)
        assertThat(updated["hasLogo"]).isEqualTo(true)
        assertThat(updated["updatedAt"]).isNotNull()

        val logoResponse = RestAssured.given()
            .`when`().get("/{id}/logo", id)
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().asByteArray()
        assertThat(logoResponse).isEqualTo(newLogo)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `updating without a logo keeps the existing logo`() {
        val id = createApplication("Paperless", withLogo = true)

        RestAssured.given()
            .multiPart("name", "Paperless-ngx")
            .multiPart(utf8Part("category", "Documents"))
            .multiPart("description", "GED")
            .multiPart("url", "https://paperless.example.org")
            .multiPart("requiresVpn", "false")
            .`when`().put("/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)

        val logoResponse = RestAssured.given()
            .`when`().get("/{id}/logo", id)
            .then().statusCode(Response.Status.OK.statusCode)
            .contentType("image/png")
            .extract().asByteArray()
        assertThat(logoResponse).isEqualTo(pngBytes)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `updating an unknown application returns 404`() {
        RestAssured.given()
            .multiPart("name", "Ghost")
            .multiPart("category", "Médias")
            .multiPart("description", "desc")
            .multiPart("url", "https://ghost.example.org")
            .multiPart("requiresVpn", "false")
            .`when`().put("/{id}", "00000000-0000-0000-0000-000000000000")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin cannot update an application with missing fields`() {
        val id = createApplication("Vaultwarden")

        RestAssured.given()
            .multiPart("name", "Vaultwarden")
            .`when`().put("/{id}", id)
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `regular user cannot update an application`() {
        RestAssured.given()
            .multiPart("name", "Nope")
            .multiPart("category", "Médias")
            .multiPart("description", "desc")
            .multiPart("url", "https://nope.example.org")
            .multiPart("requiresVpn", "false")
            .`when`().put("/{id}", "00000000-0000-0000-0000-000000000000")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `admin can delete an application`() {
        val id = createApplication("Uptime", withLogo = true)

        RestAssured.given()
            .`when`().delete("/{id}", id)
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val applications = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(applications.getList<Map<String, Any>>("").none { it["id"] == id }).isTrue()

        RestAssured.given()
            .`when`().get("/{id}/logo", id)
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `deleting an unknown application returns 404`() {
        RestAssured.given()
            .`when`().delete("/{id}", "00000000-0000-0000-0000-000000000000")
            .then().statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `regular user cannot delete an application`() {
        RestAssured.given()
            .`when`().delete("/{id}", "00000000-0000-0000-0000-000000000000")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `regular user cannot create an application`() {
        RestAssured.given()
            .multiPart("name", "Forbidden")
            .multiPart("category", "Médias")
            .multiPart("description", "desc")
            .multiPart("url", "https://forbidden.example.org")
            .multiPart("requiresVpn", "false")
            .`when`().post()
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `regular user can list applications`() {
        RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
    }

    @Test
    fun `anonymous user cannot list applications`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get()
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    @TestSecurity(user = "operator-sa", roles = ["operator"])
    fun `operator can create a managed application and its managed fields round-trip`() {
        val id = RestAssured.given()
            .multiPart("name", "Jellyseerr")
            .multiPart(utf8Part("category", "Médias"))
            .multiPart("description", "Managed by homelab-manager-operator")
            .multiPart("url", "https://jellyseerr.example.org")
            .multiPart("requiresVpn", "false")
            .multiPart("managedBy", "operator")
            .multiPart("externalId", "default/jellyseerr")
            .`when`().post()
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

        val applications = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val created = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(created["managedBy"]).isEqualTo("operator")
        assertThat(created["externalId"]).isEqualTo("default/jellyseerr")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `application created without managed fields has null managed fields`() {
        val id = createApplication("Homarr")

        val applications = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val created = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(created["managedBy"]).isNull()
        assertThat(created["externalId"]).isNull()
    }

    @Test
    @TestSecurity(user = "operator-sa", roles = ["operator"])
    fun `updating without managed fields keeps the existing managed fields`() {
        val id = RestAssured.given()
            .multiPart("name", "Radarr")
            .multiPart(utf8Part("category", "Médias"))
            .multiPart("description", "Managed by homelab-manager-operator")
            .multiPart("url", "https://radarr.example.org")
            .multiPart("requiresVpn", "true")
            .multiPart("managedBy", "operator")
            .multiPart("externalId", "default/radarr")
            .`when`().post()
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

        // Simule une édition depuis l'admin UI, qui n'envoie pas les champs managed
        RestAssured.given()
            .multiPart("name", "Radarr Films")
            .multiPart(utf8Part("category", "Médias"))
            .multiPart("description", "Gestion des films")
            .multiPart("url", "https://radarr.example.org")
            .multiPart("requiresVpn", "true")
            .`when`().put("/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)

        val applications = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val updated = applications.getList<Map<String, Any>>("").first { it["id"] == id }
        assertThat(updated["name"]).isEqualTo("Radarr Films")
        assertThat(updated["managedBy"]).isEqualTo("operator")
        assertThat(updated["externalId"]).isEqualTo("default/radarr")
    }

    @Test
    @TestSecurity(user = "operator-sa", roles = ["operator"])
    fun `operator can update and delete an application`() {
        val id = createApplication("Sonarr")

        RestAssured.given()
            .multiPart("name", "Sonarr")
            .multiPart(utf8Part("category", "Médias"))
            .multiPart("description", "Séries")
            .multiPart("url", "https://sonarr.example.org")
            .multiPart("requiresVpn", "true")
            .`when`().put("/{id}", id)
            .then().statusCode(Response.Status.OK.statusCode)

        RestAssured.given()
            .`when`().delete("/{id}", id)
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
    }
}
