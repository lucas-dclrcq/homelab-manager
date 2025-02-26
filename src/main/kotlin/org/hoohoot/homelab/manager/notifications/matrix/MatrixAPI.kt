package org.hoohoot.homelab.manager.notifications.matrix

import io.smallrye.mutiny.Uni
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/_matrix/client")
@RegisterRestClient(configKey = "matrix-api")
@ClientHeaderParam(name = "Authorization", value = ["Bearer \${matrix.access_token}"])
interface MatrixAPI {
    @PUT
    @Path("/r0/rooms/{matrixRoom}/send/m.room.message/{transactionId}")
    fun sendMessage(
        @PathParam("matrixRoom") matrixRoom: String?,
        @PathParam("transactionId") transactionId: String?,
        message: MatrixMessage?
    ): Uni<Response>
}