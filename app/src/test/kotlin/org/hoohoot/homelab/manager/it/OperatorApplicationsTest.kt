package org.hoohoot.homelab.manager.it

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val API_KEY_HEADER = "X-Api-Key"
private const val TEST_API_KEY = "test-operator-key"
private const val OPERATOR_APPLICATIONS = "/api/operator/applications"

@QuarkusTest
internal class OperatorApplicationsTest {

    private fun applicationJson(name: String, externalId: String, description: String = "Managed by homelab-manager-operator") =
        """
        {
            "name": "$name",
            "category": "Médias",
            "description": "$description",
            "url": "https://${name.lowercase()}.example.org",
            "requiresVpn": false,
            "managedBy": "operator",
            "externalId": "$externalId"
        }
        """.trimIndent()

    private fun createApplication(name: String, externalId: String): String =
        RestAssured.given()
            .header(API_KEY_HEADER, TEST_API_KEY)
            .contentType(ContentType.JSON)
            .body(applicationJson(name, externalId))
            .`when`().post(OPERATOR_APPLICATIONS)
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

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
}
