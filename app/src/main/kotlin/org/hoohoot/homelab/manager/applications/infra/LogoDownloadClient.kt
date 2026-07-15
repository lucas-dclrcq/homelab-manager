package org.hoohoot.homelab.manager.applications.infra

import io.quarkus.rest.client.reactive.Url
import jakarta.ws.rs.GET
import jakarta.ws.rs.ProcessingException
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.jboss.resteasy.reactive.RestResponse
import java.time.temporal.ChronoUnit

// RestResponse (pas Response) : readEntity sur l'event loop lèverait BlockingNotAllowedException
@RegisterRestClient(configKey = "logo-download")
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface LogoDownloadClient {
    @GET
    suspend fun download(@Url url: String): RestResponse<ByteArray>
}
