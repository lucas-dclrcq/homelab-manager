package org.hoohoot.homelab.manager.it

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

@QuarkusTest
internal class FinanceRecurringJobTest {

    private fun runJob(): String =
        RestAssured.given()
            .`when`().post("/api/admin/jobs/finance-recurring/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getString("status")

    private fun createRule(label: String, startDate: LocalDate, active: Boolean = true, endDate: LocalDate? = null) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "type" to "EXPENSE",
                    "label" to label,
                    "vendor" to "FAI",
                    "amountCents" to 3999,
                    "dayOfMonth" to 1,
                    "startDate" to startDate.toString(),
                    "endDate" to endDate?.toString(),
                    "active" to active,
                )
            )
            .`when`().post("/api/admin/finances/rules")
            .then().statusCode(Response.Status.CREATED.statusCode)
    }

    private fun entriesWithLabel(label: String): List<Map<String, Any>> {
        val today = LocalDate.now()
        return listOf(today.year, today.year - 1).flatMap { year ->
            RestAssured.given()
                .`when`().get("/api/finances/entries?year=$year&pageSize=100")
                .then().statusCode(Response.Status.OK.statusCode)
                .extract().jsonPath().getList<Map<String, Any>>("items")
        }.filter { it["label"] == label }
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `job materializes past occurrences and is idempotent`() {
        val startDate = LocalDate.now().minusMonths(3).withDayOfMonth(1)
        createRule("Abonnement box récurrent", startDate)

        assertThat(runJob()).isEqualTo("SUCCESS")

        val entries = entriesWithLabel("Abonnement box récurrent")
        assertThat(entries).hasSize(4)
        assertThat(entries).allSatisfy {
            assertThat(it["source"]).isEqualTo("RECURRING")
            assertThat(it["amountCents"]).isEqualTo(3999)
        }
        assertThat(entries.map { it["period"] }).doesNotHaveDuplicates()

        assertThat(runJob()).isEqualTo("SUCCESS")
        assertThat(entriesWithLabel("Abonnement box récurrent")).hasSize(4)
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `inactive rule and end date are honored`() {
        val startDate = LocalDate.now().minusMonths(3).withDayOfMonth(1)
        createRule("Règle inactive", startDate, active = false)
        createRule("Règle terminée", startDate, endDate = startDate.plusMonths(1))

        assertThat(runJob()).isEqualTo("SUCCESS")

        assertThat(entriesWithLabel("Règle inactive")).isEmpty()
        assertThat(entriesWithLabel("Règle terminée")).hasSize(2)
    }
}
