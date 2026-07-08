package org.hoohoot.homelab.manager.notifications.infra.giphy

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import io.quarkus.rest.client.reactive.Url
import jakarta.ws.rs.GET
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

/** Les gifs sont servis depuis des URLs absolues variables : @Url remplace la base à chaque appel. */
@RegisterRestClient(configKey = "giphy-media")
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface GifDownloadClient {
    @GET
    suspend fun download(@Url url: String): ByteArray
}
