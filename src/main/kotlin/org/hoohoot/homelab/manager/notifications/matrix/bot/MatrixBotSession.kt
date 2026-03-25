package org.hoohoot.homelab.manager.notifications.matrix.bot

import io.ktor.http.*
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.room
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.core.model.RoomAliasId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.StateEventContent
import net.folivo.trixnity.core.model.events.m.room.CanonicalAliasEventContent
import net.folivo.trixnity.core.serialization.events.contentType

@ApplicationScoped
class MatrixBotSession(private val config: MatrixBotConfiguration) {

    private lateinit var matrixClient: MatrixClient

    val client get() = matrixClient
    val room get() = matrixClient.room
    val roomApi get() = matrixClient.api.room
    val userId get() = matrixClient.userId

    fun isSameUser(userId: UserId) = userId == matrixClient.userId

    suspend fun initialize() {
        Log.info("Creating Matrix client")
        matrixClient =
            MatrixClient.fromStore(createRepositoriesModule(config), createMediaStore(config)).getOrThrow()
                ?: MatrixClient.login(
                    baseUrl = Url(config.baseUrl()),
                    identifier = IdentifierType.User(config.username()),
                    password = config.password(),
                    repositoriesModule = createRepositoriesModule(config),
                    mediaStore = createMediaStore(config),
                    initialDeviceDisplayName = "An interesting bot",
                ).getOrThrow()
    }

    suspend fun resolvePublicRoomIdOrNull(publicRoomAlias: String): RoomId? {
        val roomAlias = RoomAliasId(publicRoomAlias)

        val allKnownRooms = roomApi.getJoinedRooms().getOrThrow()
        for (room in allKnownRooms) {
            val aliasState = getStateEvent<CanonicalAliasEventContent>(room).getOrNull() ?: continue
            if (aliasState.alias == roomAlias) {
                return room
            }
            if (roomAlias in (aliasState.aliases ?: emptySet())) {
                return room
            }
        }
        return null
    }

    private suspend inline fun <reified C : StateEventContent> getStateEvent(roomId: RoomId): Result<C> {
        val type = roomApi.contentMappings.state.contentType(C::class)
        @Suppress("UNCHECKED_CAST")
        return matrixClient.api.room.getStateEvent(type, roomId) as Result<C>
    }
}
