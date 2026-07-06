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
}
