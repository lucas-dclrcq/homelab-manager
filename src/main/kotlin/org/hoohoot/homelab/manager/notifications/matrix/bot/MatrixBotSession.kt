package org.hoohoot.homelab.manager.notifications.matrix.bot

import io.ktor.http.*
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.create
import de.connect2x.trixnity.client.room
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.classicLoginWithPassword
import de.connect2x.trixnity.clientserverapi.model.authentication.IdentifierType
import de.connect2x.trixnity.core.model.RoomAliasId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.StateEventContent
import de.connect2x.trixnity.core.model.events.m.room.CanonicalAliasEventContent

@ApplicationScoped
class MatrixBotSession(private val config: MatrixBotConfiguration, private val dataSource: javax.sql.DataSource) {

    private lateinit var matrixClient: MatrixClient

    val client get() = matrixClient
    val room get() = matrixClient.room
    val roomApi get() = matrixClient.api.room
    val userId get() = matrixClient.userId

    fun isSameUser(userId: UserId) = userId == matrixClient.userId

    suspend fun initialize() {
        Log.info("Creating Matrix client")
        matrixClient = MatrixClient.create(
            repositoriesModule = createRepositoriesModule(dataSource),
            mediaStoreModule = createMediaStore(config),
            cryptoDriverModule = createCryptoDriverModule(),
            authProviderData = MatrixClientAuthProviderData.classicLoginWithPassword(
                baseUrl = Url(config.baseUrl()),
                identifier = IdentifierType.User(config.username()),
                password = config.password(),
                initialDeviceDisplayName = "An interesting bot",
            ).getOrThrow(),
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
        val type = roomApi.contentMappings.state.first { it.kClass == C::class }.type
        @Suppress("UNCHECKED_CAST")
        return matrixClient.api.room.getStateEventContent(type, roomId) as Result<C>
    }
}
