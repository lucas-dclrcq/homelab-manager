package org.hoohoot.homelab.manager.shared.prometheus

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.time.temporal.ChronoUnit

data class PrometheusResult(
    @field:JsonProperty("metric")
    val metric: Map<String, String> = emptyMap(),

    // Vecteur instantané : [timestamp epoch, "valeur en string"]
    @field:JsonProperty("value")
    val value: List<Any> = emptyList(),
) {
    fun doubleValue(): Double? = (value.getOrNull(1) as? String)?.toDoubleOrNull()
}

data class PrometheusData(
    @field:JsonProperty("resultType")
    val resultType: String = "",

    @field:JsonProperty("result")
    val result: List<PrometheusResult> = emptyList(),
)

data class PrometheusQueryResponse(
    @field:JsonProperty("status")
    val status: String = "",

    @field:JsonProperty("data")
    val data: PrometheusData = PrometheusData(),
) {
    fun firstValue(): Double? = data.result.firstOrNull()?.doubleValue()
}

@Path("/api/v1")
@RegisterRestClient(configKey = "prometheus-api")
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface PrometheusRestClient {
    @GET
    @Path("/query")
    suspend fun query(
        @QueryParam("query") query: String,
        @QueryParam("time") time: String?,
    ): PrometheusQueryResponse
}
