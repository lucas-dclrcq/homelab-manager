package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/_matrix/client")
@RegisterRestClient(configKey = "matrix-api")
@ClientHeaderParam(name = "Authorization", value = ["Bearer \${matrix.access_token}"])
@RegisterProvider(value = MatrixApiObjectMapper::class)
interface MatrixRestClient {
    @PUT
    @Path("/r0/rooms/{matrixRoom}/send/m.room.message/{transactionId}")
    suspend fun sendMessage(
        @PathParam("matrixRoom") matrixRoom: String?,
        @PathParam("transactionId") transactionId: String?,
        message: MatrixMessage?
    ):  MatrixMessageResponse
}