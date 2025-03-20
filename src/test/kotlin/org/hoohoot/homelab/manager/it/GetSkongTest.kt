package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.enterprise.inject.Default
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.datetime.Instant
import org.hamcrest.Matchers.equalTo
import org.hoohoot.homelab.manager.infrastructure.api.resources.RandomResource
import org.hoohoot.homelab.manager.infrastructure.time.TimeService
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@QuarkusTest
@TestHTTPEndpoint(RandomResource::class)
@QuarkusTestResource(WiremockTestResource::class)
class GetSkongTest {
    @Inject
    @field:Default
    lateinit var timeService: TimeService


    @ParameterizedTest
    @CsvSource(
        """doubter;🔴 It's been 1707 days, and there is still no release date. Face it, Silksong is never coming out. Team Cherry is just a myth.""",
        """believer;🟢 Patience, my child. Silksong will come when it is ready. The longer the wait, the greater the masterpiece!""",
        delimiter = ';'
    )
    fun `should return response to skong`(skongType: String, expectedMessage: String) {
        timeService.setFixedClock(Instant.parse("2023-10-18T12:34:56Z"))

        RestAssured.given()
            .`when`().get("/$skongType")
            .then().statusCode(Response.Status.OK.statusCode)
            .body("message", equalTo(expectedMessage))
    }
}