package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.portal.resource.MeResource
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(MeResource::class)
internal class MeTest {

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `should return username and roles`() {
        val body = RestAssured.given()
            .`when`().get()
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(body.getString("username")).isEqualTo("alice")
        assertThat(body.getList<String>("roles")).contains("admin", "user")
    }

    @Test
    fun `anonymous user is rejected`() {
        RestAssured.given().redirects().follow(false)
            .`when`().get()
            .then().statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }
}
