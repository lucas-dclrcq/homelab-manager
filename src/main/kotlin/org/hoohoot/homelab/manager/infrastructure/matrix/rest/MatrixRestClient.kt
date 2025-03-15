package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.infrastructure.matrix.rest.MatrixMessage
import org.hoohoot.homelab.manager.infrastructure.matrix.rest.MatrixMessageResponse

@Path("/_matrix/client")
@RegisterRestClient(configKey = "matrix-api")
@ClientHeaderParam(name = "Authorization", value = ["Bearer \${matrix.access_token}"])
interface MatrixRestClient {
    @PUT
    @Path("/r0/rooms/{matrixRoom}/send/m.room.message/{transactionId}")
    suspend fun sendMessage(
        @PathParam("matrixRoom") matrixRoom: String?,
        @PathParam("transactionId") transactionId: String?,
        message: MatrixMessage?
    ): MatrixMessageResponse
}