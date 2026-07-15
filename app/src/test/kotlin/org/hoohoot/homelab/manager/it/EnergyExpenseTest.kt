package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private const val AVERAGE_WATTS = 279.7
private const val KWH_PRICE = 0.2016

@QuarkusTest
internal class EnergyExpenseTest {

    @Inject
    lateinit var wireMock: WireMock

    @BeforeEach
    fun setUp() {
        wireMock.resetMappings()
        wireMock.register(
            get(urlPathEqualTo("/api/v1/query")).willReturn(
                okJson("""{"status":"success","data":{"resultType":"vector","result":[{"metric":{},"value":[1750000000,"$AVERAGE_WATTS"]}]}}""")
            )
        )
    }

    private fun runJob(): String =
        RestAssured.given()
            .`when`().post("/api/admin/jobs/energy-expense/run")
            .then().statusCode(Response.Status.OK.statusCode)
            .extract().jsonPath().getString("status")

    private fun updateKwhPrice(price: Double?) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(mapOf("kwhPrice" to price))
            .`when`().put("/api/admin/finances/settings")
            .then().statusCode(Response.Status.OK.statusCode)
    }

    private fun energyEntries(): List<Map<String, Any>> {
        val today = LocalDate.now()
        return listOf(today.year, today.year - 1).flatMap { year ->
            RestAssured.given()
                .`when`().get("/api/finances/entries?year=$year&pageSize=100")
                .then().statusCode(Response.Status.OK.statusCode)
                .extract().jsonPath().getList<Map<String, Any>>("items")
        }.filter { it["source"] == "ENERGY" }
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `job fails clearly when kwh price is not configured`() {
        updateKwhPrice(null)
        assertThat(runJob()).isEqualTo("FAILURE")
    }

    @Test
    @TestSecurity(user = "alice", roles = ["admin", "user"])
    fun `job creates one energy expense per past month and is idempotent`() {
        updateKwhPrice(KWH_PRICE)

        assertThat(runJob()).isEqualTo("SUCCESS")

        val entries = energyEntries()
        assertThat(entries).hasSize(3)
        assertThat(entries).allSatisfy { assertThat(it["type"]).isEqualTo("EXPENSE") }
        assertThat(entries.map { it["period"] }).doesNotHaveDuplicates()

        val previousMonth = YearMonth.now().minusMonths(1)
        val zone = ZoneId.systemDefault()
        val from = previousMonth.atDay(1).atStartOfDay(zone).toInstant()
        val to = previousMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
        val hours = Duration.between(from, to).toMinutes() / 60.0
        val expectedCents = BigDecimal.valueOf(AVERAGE_WATTS * hours / 1000.0)
            .multiply(BigDecimal.valueOf(KWH_PRICE))
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()

        val lastMonthEntry = entries.first { it["period"] == previousMonth.toString() }
        assertThat(lastMonthEntry["amountCents"]).isEqualTo(expectedCents)
        assertThat(lastMonthEntry["label"]).isEqualTo("Électricité $previousMonth")

        assertThat(runJob()).isEqualTo("SUCCESS")
        assertThat(energyEntries()).hasSize(3)
    }
}
