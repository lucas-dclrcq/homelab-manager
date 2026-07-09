package org.hoohoot.homelab.manager.it

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.quarkus.vertx.VertxContextSupport
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryEntity
import org.hoohoot.homelab.manager.members.infra.MemberEntity
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Année dédiée à ce test : les totaux ne sont pas pollués par les écritures
// créées par les autres classes de test (base partagée)
private const val YEAR = 2031

@QuarkusTest
internal class FinancesTest {

    private fun insertMember(displayName: String): UUID {
        val memberId = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                MemberEntity().apply {
                    id = memberId
                    username = displayName.lowercase()
                    this.displayName = displayName
                    active = true
                    createdAt = LocalDateTime.now()
                }.persist<MemberEntity>()
            }
        }
        return memberId
    }

    private fun createEntry(
        type: EntryType,
        label: String,
        amountCents: Int,
        entryDate: LocalDate,
        memberId: UUID? = null,
        vendor: String? = null,
    ): String =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "type" to type.name,
                    "label" to label,
                    "amountCents" to amountCents,
                    "entryDate" to entryDate.toString(),
                    "memberId" to memberId?.toString(),
                    "vendor" to vendor,
                )
            )
            .`when`().post("/api/admin/finances/entries")
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `summary and monthly breakdown reflect created entries`() {
        val paul = insertMember("Paul Summary")
        createEntry(EntryType.CONTRIBUTION, "Cotisation Paul", 1500, LocalDate.of(YEAR, 1, 15), memberId = paul)
        createEntry(EntryType.CONTRIBUTION, "Cotisation Paul", 1500, LocalDate.of(YEAR, 2, 15), memberId = paul)
        createEntry(EntryType.EXPENSE, "Disque dur", 30000, LocalDate.of(YEAR, 1, 20), vendor = "server part deals")

        val summary = RestAssured.given()
            .`when`().get("/api/finances/summary?year=$YEAR")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        assertThat(summary.getInt("year")).isEqualTo(YEAR)
        assertThat(summary.getLong("totalContributionsCents")).isEqualTo(3000L)
        assertThat(summary.getLong("totalExpensesCents")).isEqualTo(30000L)
        assertThat(summary.getLong("balanceCents")).isEqualTo(-27000L)

        val monthly = RestAssured.given()
            .`when`().get("/api/finances/monthly?year=$YEAR")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val months = monthly.getList<Map<String, Any>>("")
        assertThat(months).hasSize(12)
        assertThat(months[0]["contributionsCents"]).isEqualTo(1500)
        assertThat(months[0]["expensesCents"]).isEqualTo(30000)
        assertThat(months[1]["contributionsCents"]).isEqualTo(1500)
        assertThat(months[11]["contributionsCents"]).isEqualTo(0)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `entries can be filtered by type and paginated with member names`() {
        val paul = insertMember("Paul Entries")
        createEntry(EntryType.CONTRIBUTION, "Cotisation entries", 2000, LocalDate.of(YEAR, 3, 5), memberId = paul)
        createEntry(EntryType.EXPENSE, "Câbles", 4500, LocalDate.of(YEAR, 3, 10))

        val contributions = RestAssured.given()
            .`when`().get("/api/finances/entries?year=$YEAR&type=CONTRIBUTION&pageSize=50")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()

        val items = contributions.getList<Map<String, Any>>("items")
        assertThat(items).isNotEmpty
        assertThat(items).allSatisfy { assertThat(it["type"]).isEqualTo("CONTRIBUTION") }
        val cotisation = items.first { it["label"] == "Cotisation entries" }
        assertThat(cotisation["memberDisplayName"]).isEqualTo("Paul Entries")

        val paged = RestAssured.given()
            .`when`().get("/api/finances/entries?year=$YEAR&pageSize=1&page=0")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(paged.getList<Any>("items")).hasSize(1)
        assertThat(paged.getInt("pageSize")).isEqualTo(1)
    }

    @Test
    @TestSecurity(user = "bob", roles = ["user"])
    fun `simple user can read finances but not write`() {
        RestAssured.given().`when`().get("/api/finances/summary")
            .then().statusCode(Response.Status.OK.statusCode)
        RestAssured.given().`when`().get("/api/finances/monthly")
            .then().statusCode(Response.Status.OK.statusCode)
        RestAssured.given().`when`().get("/api/finances/entries")
            .then().statusCode(Response.Status.OK.statusCode)
        RestAssured.given().`when`().get("/api/members")
            .then().statusCode(Response.Status.OK.statusCode)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("type" to "EXPENSE", "label" to "x", "amountCents" to 100, "entryDate" to "2031-01-01"))
            .`when`().post("/api/admin/finances/entries")
            .then().statusCode(Response.Status.FORBIDDEN.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `contribution requires an existing member`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("type" to "CONTRIBUTION", "label" to "Sans membre", "amountCents" to 1000, "entryDate" to "2031-04-01"))
            .`when`().post("/api/admin/finances/entries")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "type" to "CONTRIBUTION",
                    "label" to "Membre inconnu",
                    "amountCents" to 1000,
                    "entryDate" to "2031-04-01",
                    "memberId" to UUID.randomUUID().toString(),
                )
            )
            .`when`().post("/api/admin/finances/entries")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `manual entry can be updated and deleted but recurring entry deletion is rejected`() {
        val id = createEntry(EntryType.EXPENSE, "À modifier", 1000, LocalDate.of(YEAR, 5, 1))

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("type" to "EXPENSE", "label" to "Modifiée", "amountCents" to 2000, "entryDate" to "2031-05-02"))
            .`when`().put("/api/admin/finances/entries/$id")
            .then().statusCode(Response.Status.OK.statusCode)
            .body("label", org.hamcrest.Matchers.equalTo("Modifiée"))
            .body("amountCents", org.hamcrest.Matchers.equalTo(2000))

        RestAssured.given()
            .`when`().delete("/api/admin/finances/entries/$id")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val recurringId = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                FinanceEntryEntity().apply {
                    this.id = recurringId
                    type = EntryType.EXPENSE
                    source = EntrySource.RECURRING
                    label = "Générée"
                    amountCents = 500
                    entryDate = LocalDate.of(YEAR, 6, 5)
                    createdAt = LocalDateTime.now()
                }.persist<FinanceEntryEntity>()
            }
        }

        RestAssured.given()
            .`when`().delete("/api/admin/finances/entries/$recurringId")
            .then().statusCode(Response.Status.CONFLICT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `recurring rules can be managed`() {
        val paul = insertMember("Paul Rules")

        val ruleId = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "type" to "CONTRIBUTION",
                    "label" to "Cotisation mensuelle Paul",
                    "amountCents" to 1500,
                    "dayOfMonth" to 5,
                    "memberId" to paul.toString(),
                    "startDate" to "2031-01-01",
                )
            )
            .`when`().post("/api/admin/finances/rules")
            .then().statusCode(Response.Status.CREATED.statusCode)
            .extract().jsonPath().getString("id")

        val rules = RestAssured.given()
            .`when`().get("/api/admin/finances/rules")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getList<Map<String, Any>>("")
        assertThat(rules.map { it["id"] }).contains(ruleId)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "type" to "CONTRIBUTION",
                    "label" to "Cotisation mensuelle Paul",
                    "amountCents" to 2000,
                    "dayOfMonth" to 5,
                    "memberId" to paul.toString(),
                    "startDate" to "2031-01-01",
                    "active" to false,
                )
            )
            .`when`().put("/api/admin/finances/rules/$ruleId")
            .then().statusCode(Response.Status.OK.statusCode)
            .body("amountCents", org.hamcrest.Matchers.equalTo(2000))
            .body("active", org.hamcrest.Matchers.equalTo(false))

        RestAssured.given()
            .`when`().delete("/api/admin/finances/rules/$ruleId")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `rule with invalid day of month is rejected`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "type" to "EXPENSE",
                    "label" to "Abonnement",
                    "amountCents" to 999,
                    "dayOfMonth" to 31,
                    "startDate" to "2031-01-01",
                )
            )
            .`when`().post("/api/admin/finances/rules")
            .then().statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `settings can be read and updated`() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("kwhPrice" to 0.2016))
            .`when`().put("/api/admin/finances/settings")
            .then().statusCode(Response.Status.OK.statusCode)

        val settings = RestAssured.given()
            .`when`().get("/api/admin/finances/settings")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath()
        assertThat(settings.getDouble("kwhPrice")).isEqualTo(0.2016)

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("kwhPrice" to null))
            .`when`().put("/api/admin/finances/settings")
            .then().statusCode(Response.Status.OK.statusCode)
    }
}
