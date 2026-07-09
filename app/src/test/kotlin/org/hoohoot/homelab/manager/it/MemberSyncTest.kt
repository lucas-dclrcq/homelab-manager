package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class MemberSyncTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        wireMock.resetMappings()
    }

    private fun stubUsersPage(page: Int, next: Int, users: String) {
        wireMock.register(
            get(urlPathEqualTo("/api/v3/core/users/"))
                .withQueryParam("page", equalTo(page.toString()))
                .willReturn(okJson("""{"pagination": {"next": $next, "total_pages": 2, "count": 3}, "results": [$users]}"""))
        )
    }

    private fun runSync(): String =
        RestAssured.given()
            .`when`().post("/api/admin/jobs/member-sync/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getString("status")

    private fun fetchMembers(): List<Map<String, Any>> =
        RestAssured.given()
            .`when`().get("/api/members")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList("")

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `sync pages through authentik, upserts and deactivates missing members`() {
        stubUsersPage(
            page = 1, next = 2,
            users = """{"pk": 9101, "username": "sync-paul", "name": "Paul Sync", "email": "paul@hoohoot.org", "is_active": true},
                       {"pk": 9102, "username": "sync-mara", "name": "Mara Sync", "is_active": true}"""
        )
        stubUsersPage(
            page = 2, next = 0,
            users = """{"pk": 9103, "username": "sync-noah", "name": "", "is_active": true}"""
        )

        assertThat(runSync()).isEqualTo("SUCCESS")

        val members = fetchMembers().filter { (it["username"] as String).startsWith("sync-") }
        assertThat(members).hasSize(3)
        val paul = members.first { it["username"] == "sync-paul" }
        assertThat(paul["displayName"]).isEqualTo("Paul Sync")
        assertThat(paul["email"]).isEqualTo("paul@hoohoot.org")
        assertThat(paul["fromAuthentik"]).isEqualTo(true)
        // name vide → fallback sur le username
        assertThat(members.first { it["username"] == "sync-noah" }["displayName"]).isEqualTo("sync-noah")

        // Re-run à l'identique : idempotent, pas de doublon
        assertThat(runSync()).isEqualTo("SUCCESS")
        assertThat(fetchMembers().filter { (it["username"] as String).startsWith("sync-") }).hasSize(3)

        // Mara disparaît de l'annuaire : désactivée mais conservée (historique des cotisations)
        wireMock.resetMappings()
        stubUsersPage(
            page = 1, next = 0,
            users = """{"pk": 9101, "username": "sync-paul", "name": "Paul Sync", "email": "paul@hoohoot.org", "is_active": true},
                       {"pk": 9103, "username": "sync-noah", "name": "", "is_active": true}"""
        )
        assertThat(runSync()).isEqualTo("SUCCESS")

        val afterRemoval = fetchMembers().filter { (it["username"] as String).startsWith("sync-") }
        assertThat(afterRemoval).hasSize(3)
        assertThat(afterRemoval.first { it["username"] == "sync-mara" }["active"]).isEqualTo(false)
        assertThat(afterRemoval.first { it["username"] == "sync-paul" }["active"]).isEqualTo(true)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `sync fails without deactivating anyone when authentik returns no user`() {
        stubUsersPage(page = 1, next = 0, users = "")

        assertThat(runSync()).isEqualTo("FAILURE")
    }
}
