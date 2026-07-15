package org.hoohoot.homelab.manager.shared.matrix.bot

import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.vertx.core.Vertx
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import de.connect2x.trixnity.clientserverapi.client.SyncState
import de.connect2x.trixnity.core.model.events.*
import de.connect2x.trixnity.core.model.events.m.ReactionEventContent
import de.connect2x.trixnity.core.model.events.m.room.MemberEventContent
import de.connect2x.trixnity.core.model.events.m.room.Membership
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import de.connect2x.trixnity.core.subscribeContent
import org.hoohoot.homelab.manager.shared.matrix.bot.reactions.ReactionBotHandlers
import org.hoohoot.homelab.manager.shared.vertx.QuarkusClassLoaderElement
import org.hoohoot.homelab.manager.shared.vertx.newSafeVertxDispatcher

enum class MatrixBotStatus { DISABLED, CONNECTING, RUNNING, STOPPED }

@ApplicationScoped
class MatrixBotLifecycle(
    private val session: MatrixBotSession,
    private val dispatcher: MatrixBotCommandDispatcher,
    private val reactionHandlers: ReactionBotHandlers,
    private val config: MatrixBotConfiguration,
    private val vertx: Vertx,
) {

    private val runningTimestamp = Clock.System.now()
    private lateinit var quarkusClassLoader: ClassLoader
    private val botScope = CoroutineScope(SupervisorJob() + CoroutineName("matrix-bot"))
    private var startupJob: Job? = null

    @Volatile
    private var state = MatrixBotStatus.STOPPED

    val status: MatrixBotStatus get() = state

    @Volatile
    private var syncRunning = false
    private var initialized = false

    fun onStart(@Observes event: StartupEvent) {
        if (!config.enabled()) {
            state = MatrixBotStatus.DISABLED
            return
        }
        if (syncRunning) return

        quarkusClassLoader = Thread.currentThread().contextClassLoader
        state = MatrixBotStatus.CONNECTING

        // Init en arrière-plan avec retry : un Matrix indisponible ne bloque pas le démarrage
        // de l'app. Dispatchers.IO car MatrixClient.create fait du JDBC bloquant (schéma Exposed).
        startupJob = botScope.launch(Dispatchers.IO + QuarkusClassLoaderElement(quarkusClassLoader)) {
            var retryDelay = INITIAL_RETRY_DELAY
            while (isActive) {
                try {
                    if (!initialized) {
                        session.initialize()
                        subscribeSyncEvents()
                        initialized = true
                        Log.info("Matrix bot initialized.")
                    }
                    session.client.startSync()
                    syncRunning = true
                    state = MatrixBotStatus.RUNNING
                    Log.info("Matrix bot sync started.")
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.error("Failed to initialize Matrix bot, retrying in $retryDelay", e)
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(MAX_RETRY_DELAY)
                }
            }
        }
    }

    fun onStop(@Observes event: ShutdownEvent) {
        if (!config.enabled()) return

        runBlocking {
            startupJob?.cancelAndJoin()
            if (syncRunning) {
                Log.info("Stopping Matrix bot sync...")
                session.client.stopSync()
                syncRunning = false
                Log.info("Matrix bot stopped.")
            }
            withTimeoutOrNull(5.seconds) {
                botScope.coroutineContext.job.children.toList().joinAll()
            }
            botScope.cancel()
        }
        state = MatrixBotStatus.STOPPED
    }

    fun currentSyncState(): SyncState? =
        if (syncRunning) session.client.syncState.value else null

    private fun subscribeSyncEvents() {
        session.client.api.sync.subscribeContent<MemberEventContent> { event ->
            launchEventHandler("join handling") {
                handleJoinEvent(event)
            }
        }

        session.client.api.sync.subscribeContent<RoomMessageEventContent> { event ->
            launchEventHandler("message handling") {
                if (isValidEventFromUser(event)) {
                    dispatcher.dispatch(event)
                }
            }
        }

        session.client.api.sync.subscribeContent<ReactionEventContent> { event ->
            launchEventHandler("reaction handling") {
                if (isValidEventFromUser(event)) {
                    dispatchReaction(event)
                }
            }
        }
    }

    private fun launchEventHandler(what: String, block: suspend () -> Unit) {
        botScope.launch(newSafeVertxDispatcher(vertx) + QuarkusClassLoaderElement(quarkusClassLoader)) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error("Matrix bot: $what failed", e)
            }
        }
    }

    private suspend fun handleJoinEvent(event: ClientEvent<MemberEventContent>) {
        val roomId = event.roomIdOrNull ?: return
        val stateKey = event.stateKeyOrNull ?: return

        if (stateKey != session.userId.full) return
        if (!config.isUser(event.senderOrNull) || event.senderOrNull == session.userId) return
        if (event.content.membership != Membership.INVITE) return

        val room = session.room.getById(roomId).firstWithTimeout { it != null } ?: return
        if (room.membership != Membership.INVITE) return

        Log.info("Joining Room: $roomId by invitation of ${event.senderOrNull?.full ?: "Unknown User"}")
        session.roomApi
            .joinRoom(roomId)
            .onFailure { Log.error("Could not join room $roomId: ${it.message}", it) }
    }

    private suspend fun dispatchReaction(event: ClientEvent<ReactionEventContent>) {
        val roomId = event.roomIdOrNull ?: return
        val sender = event.senderOrNull ?: return
        val relatesTo = event.content.relatesTo ?: return
        val key = relatesTo.key ?: return

        // Un handler qui échoue ne doit pas empêcher les autres de traiter la réaction
        for (handler in reactionHandlers.all()) {
            try {
                handler.handle(session, sender, roomId, relatesTo.eventId, key)
            } catch (e: Exception) {
                Log.error("Reaction handler ${handler.javaClass.simpleName} failed", e)
            }
        }
    }

    private fun isValidEventFromUser(event: ClientEvent<*>): Boolean {
        if (!config.isUser(event.senderOrNull)) return false
        if (event.senderOrNull == session.userId) return false
        val timeOfOrigin = event.originTimestampOrNull
        return !(timeOfOrigin == null || Instant.fromEpochMilliseconds(timeOfOrigin) < runningTimestamp)
    }

    companion object {
        private val INITIAL_RETRY_DELAY = 5.seconds
        private val MAX_RETRY_DELAY = 60.seconds
    }
}
