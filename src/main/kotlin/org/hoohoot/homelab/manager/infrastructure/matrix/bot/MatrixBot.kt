package org.hoohoot.homelab.manager.infrastructure.matrix.bot

import io.ktor.http.*
import io.quarkus.logging.Log
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.react
import net.folivo.trixnity.clientserverapi.client.SyncState
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.core.ClientEventEmitter
import net.folivo.trixnity.core.Subscriber
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomAliasId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.*
import net.folivo.trixnity.core.model.events.m.room.CanonicalAliasEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.Membership
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.core.serialization.events.contentType
import net.folivo.trixnity.core.subscribeContent
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.*
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.prefixed.PrefixedBotCommands
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.regex.RegexBotCommands
import kotlin.reflect.KClass

@ApplicationScoped
class MatrixBot(
    private val config: MatrixBotConfiguration,
    private val prefixedBotCommands: PrefixedBotCommands,
    private val regexBotCommands: RegexBotCommands
) {

    private val runningTimestamp = Clock.System.now()
    private val validStates = listOf(SyncState.RUNNING, SyncState.INITIAL_SYNC, SyncState.STARTED)
    private val runningLock = Semaphore(1, 1)
    private var running: Boolean = false

    private var logout: Boolean = false

    private lateinit var matrixClient: MatrixClient

    fun onStart(@Observes event: StartupEvent) {
        if (config.enabled().not()) return

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
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

            Log.info("Starting Matrix bot...")



            matrixClient.api.sync.subscribeContent { event -> handleJoinEvent(event) }

            this@MatrixBot.subscribeContent { event -> handleCommand(event) }

            this@MatrixBot.startBlocking()
        }
    }

    fun room() = matrixClient.room

    fun roomApi() = matrixClient.api.room

    fun contentMappings() = matrixClient.api.room.contentMappings

    suspend fun getStateEvent(
        type: String,
        roomId: RoomId
    ): Result<StateEventContent> = matrixClient.api.room.getStateEvent(type, roomId)

    /**
     * Starts the bot. Note that this method blocks until [quit] will be executed from another thread.
     * @return true if the bot was logged out, false if the bot simply quit.
     */
    private suspend fun startBlocking(): Boolean {
        running = true
        registerShutdownHook()

        Log.info("Starting Sync!")
        matrixClient.startSync()
        delay(1000)

        Log.info("Waiting for events ..")
        runningLock.acquire()

        Log.info("Shutting down!")
        while (matrixClient.syncState.value in validStates) {
            delay(500)
        }
        running = false
        if (logout) {
            matrixClient.api.authentication.logoutAll()
        }

        matrixClient.stopSync()
        return logout
    }

    private suspend inline fun <reified C : StateEventContent> getStateEvent(roomId: RoomId): Result<C> {
        val type = contentMappings().state.contentType(C::class)
        @Suppress("UNCHECKED_CAST")
        return getStateEvent(type, roomId) as Result<C>
    }

    fun self() = matrixClient.userId

    fun <T : EventContent> subscribeContent(
        clazz: KClass<T>,
        subscriber: Subscriber<ClientEvent<T>>,
        listenNonUsers: Boolean = false,
        listenBotEvents: Boolean = false
    ) {
        matrixClient.api.sync.subscribeContent(clazz, ClientEventEmitter.Priority.DEFAULT) { event ->
            if (isValidEventFromUser(
                    event,
                    listenNonUsers,
                    listenBotEvents
                )
            ) {
                subscriber(event)
            }
        }
    }

    suspend fun resolvePublicRoomIdOrNull(publicRoomAlias: String): RoomId? {
        val roomAlias = RoomAliasId(publicRoomAlias)

        val allKnownRooms = roomApi().getJoinedRooms().getOrThrow()
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

    private inline fun <reified T : EventContent> subscribeContent(
        listenNonUsers: Boolean = false,
        listenBotEvents: Boolean = false,
        noinline subscriber: Subscriber<ClientEvent<T>>
    ) {
        subscribeContent(T::class, subscriber, listenNonUsers, listenBotEvents)
    }

    suspend fun quit(logout: Boolean = false) {
        this.logout = logout
        matrixClient.stopSync()
        runningLock.release()
    }

    private fun isValidEventFromUser(
        event: ClientEvent<*>,
        listenNonUsers: Boolean,
        listenBotEvents: Boolean
    ): Boolean {
        if (!config.isUser(event.senderOrNull) && !listenNonUsers) return false
        if (event.senderOrNull == matrixClient.userId && !listenBotEvents) return false
        val timeOfOrigin = event.originTimestampOrNull
        return !(timeOfOrigin == null || Instant.fromEpochMilliseconds(timeOfOrigin) < runningTimestamp)
    }

    private suspend fun handleJoinEvent(event: ClientEvent<MemberEventContent>) {
        val roomId = event.roomIdOrNull ?: return
        val stateKey = event.stateKeyOrNull ?: return

        if (stateKey != self().full) return

        if (!config.isUser(event.senderOrNull) || event.senderOrNull == self()) return

        if (event.content.membership != Membership.INVITE) {
            return
        }

        // Check if already joined ..
        val room = matrixClient.room.getById(roomId).firstWithTimeout { it != null } ?: return
        if (room.membership != Membership.INVITE) return

        Log.info("Joining Room: $roomId by invitation of ${event.senderOrNull?.full ?: "Unknown User"}")
        matrixClient.api.room
            .joinRoom(roomId)
            .onFailure { Log.error("Could not join room $roomId: ${it.message}", it) }
    }

    private fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    runBlocking { if (running) quit() }
                }
            }
        )
    }

    private suspend fun handleCommand(
        event: ClientEvent<RoomMessageEventContent>,
        defaultCommand: String? = null
    ) {
        val roomId = event.roomIdOrNull ?: return
        val sender = event.senderOrNull ?: return
        val eventId = event.idOrNull ?: return
        val content = event.content
        if (content is RoomMessageEventContent.TextBased.Text) {
            val hasExecutedPrefixedCommand = executePrefixedCommand(sender, roomId, eventId, content, config, defaultCommand)

            if (!hasExecutedPrefixedCommand) {
                executeRegexCommand(sender, roomId, eventId, content)
            }
        }
    }

    private suspend fun executePrefixedCommand(
        sender: UserId,
        roomId: RoomId,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text,
        config: MatrixBotConfiguration,
        defaultCommand: String?
    ): Boolean {
        var message = textEvent.body
        if (!message.startsWith("!${config.prefix()}")) {
            return false
        }
        message = message.substring("!${config.prefix()}".length).trim()

        val command = message.split(Regex(" "), 2)[0]
        var parameters = message.substring(command.length).trim()

        var commandToExecute = prefixedBotCommands.find(command)

        if (commandToExecute == null && defaultCommand != null) {
            commandToExecute = prefixedBotCommands.find(defaultCommand)
            parameters = message
        }

        if (commandToExecute == null) {
            return false
        }

        if (commandToExecute.autoAcknowledge) {
            this.room().sendMessage(roomId) {
                react(textEventId, MatrixBotCommand.ACK_EMOJI)
            }
        }

        commandToExecute.execute(this, sender, roomId, parameters, textEventId, textEvent)
        return true
    }

    private suspend fun executeRegexCommand(
        sender: UserId,
        roomId: RoomId,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val message = textEvent.body

        val commandToExecute = regexBotCommands.find(message)

        if (commandToExecute == null) {
            return
        }

        if (commandToExecute.autoAcknowledge) {
            this.room().sendMessage(roomId) {
                react(textEventId, MatrixBotCommand.ACK_EMOJI)
            }
        }

        commandToExecute.execute(this, sender, roomId, message, textEventId, textEvent)
    }
}