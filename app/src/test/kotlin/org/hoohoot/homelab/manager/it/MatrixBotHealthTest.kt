package org.hoohoot.homelab.manager.it

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.Duration

@QuarkusTest
internal class MatrixBotHealthTest {

    @Test
    fun `wellness check should report matrix bot up once synced`() {
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted {
            val body = RestAssured.given()
                .`when`().get("/q/health/well")
                .then().statusCode(200)
                .extract().jsonPath()

            val botCheck = body.getList<Map<String, Any>>("checks")
                .firstOrNull { it["name"] == "matrix-bot" }

            assertThat(botCheck).isNotNull
            assertThat(botCheck!!["status"]).isEqualTo("UP")
            @Suppress("UNCHECKED_CAST")
            val data = botCheck["data"] as Map<String, Any>
            assertThat(data["status"]).isEqualTo("running")
            assertThat(data["syncState"]).isNotNull
        }
    }
}
