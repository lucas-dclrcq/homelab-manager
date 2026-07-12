package org.hoohoot.homelab.manager.shared.matrix.bot

import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ThreadContextElement
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import de.connect2x.trixnity.core.model.events.*
import de.connect2x.trixnity.core.model.events.m.ReactionEventContent
import de.connect2x.trixnity.core.model.events.m.room.MemberEventContent
import de.connect2x.trixnity.core.model.events.m.room.Membership
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import de.connect2x.trixnity.core.subscribeContent
import org.hoohoot.homelab.manager.shared.matrix.bot.reactions.ReactionBotHandlers
import kotlin.coroutines.CoroutineContext

@ApplicationScoped
class MatrixBotLifecycle(
    private val session: MatrixBotSession,
    private val dispatcher: MatrixBotCommandDispatcher,
    private val reactionHandlers: ReactionBotHandlers,
    private val config: MatrixBotConfiguration
) {

    private val runningTimestamp = Clock.System.now()
    private lateinit var quarkusClassLoader: ClassLoader

    @Volatile
    private var syncRunning = false
    private var initialized = false

    fun onStart(@Observes event: StartupEvent) {
        if (!config.enabled() || syncRunning) return

        quarkusClassLoader = Thread.currentThread().contextClassLoader

        runBlocking {
            if (!initialized) {
                try {
                    session.initialize()

                    session.client.api.sync.subscribeContent<MemberEventContent> { event ->
                        withQuarkusClassLoader {
                            handleJoinEvent(event)
                        }
                    }

                    session.client.api.sync.subscribeContent<RoomMessageEventContent> { event ->
                        withQuarkusClassLoader {
                            if (isValidEventFromUser(event)) {
                                dispatcher.dispatch(event)
                            }
                        }
                    }

                    session.client.api.sync.subscribeContent<ReactionEventContent> { event ->
                        withQuarkusClassLoader {
                            if (isValidEventFromUser(event)) {
                                dispatchReaction(event)
                            }
                        }
                    }

                    initialized = true
                    Log.info("Matrix bot initialized.")
                } catch (e: Exception) {
                    Log.error("Failed to initialize Matrix bot.", e)
                    return@runBlocking
                }
            }

            Log.info("Starting Sync!")
            session.client.startSync()
            syncRunning = true
            Log.info("Matrix bot sync started.")
        }
    }

    fun onStop(@Observes event: ShutdownEvent) {
        if (!config.enabled()) return

        runBlocking {
            if (syncRunning) {
                Log.info("Stopping Matrix bot sync...")
                session.client.stopSync()
                syncRunning = false
                Log.info("Matrix bot stopped.")
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

    private suspend fun <T> withQuarkusClassLoader(block: suspend () -> T): T =
        withContext(ClassLoaderContextElement(quarkusClassLoader)) { block() }

    private class ClassLoaderContextElement(
        private val classLoader: ClassLoader
    ) : ThreadContextElement<ClassLoader> {
        companion object Key : CoroutineContext.Key<ClassLoaderContextElement>
        override val key: CoroutineContext.Key<ClassLoaderContextElement> = Key

        override fun updateThreadContext(context: CoroutineContext): ClassLoader {
            val old = Thread.currentThread().contextClassLoader
            Thread.currentThread().contextClassLoader = classLoader
            return old
        }

        override fun restoreThreadContext(context: CoroutineContext, oldState: ClassLoader) {
            Thread.currentThread().contextClassLoader = oldState
        }
    }
}
