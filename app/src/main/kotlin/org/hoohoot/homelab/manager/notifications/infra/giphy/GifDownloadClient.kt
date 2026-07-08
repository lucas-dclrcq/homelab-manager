package org.hoohoot.homelab.manager.notifications.infra.giphy

import io.quarkus.rest.client.reactive.Url
import jakarta.ws.rs.GET
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

/** Les gifs sont servis depuis des URLs absolues variables : @Url remplace la base à chaque appel. */
@RegisterRestClient(configKey = "giphy-media")
interface GifDownloadClient {
    @GET
    suspend fun download(@Url url: String): ByteArray
}
