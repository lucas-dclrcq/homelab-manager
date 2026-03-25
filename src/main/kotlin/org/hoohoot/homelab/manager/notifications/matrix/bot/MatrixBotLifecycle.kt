package org.hoohoot.homelab.manager.notifications.matrix.bot

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
import net.folivo.trixnity.core.model.events.*
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.Membership
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.core.subscribeContent
import kotlin.coroutines.CoroutineContext

@ApplicationScoped
class MatrixBotLifecycle(
    private val session: MatrixBotSession,
    private val dispatcher: MatrixBotCommandDispatcher,
    private val config: MatrixBotConfiguration
) {

    private val runningTimestamp = Clock.System.now()
    private lateinit var quarkusClassLoader: ClassLoader

    fun onStart(@Observes event: StartupEvent) {
        if (!config.enabled()) return

        quarkusClassLoader = Thread.currentThread().contextClassLoader

        runBlocking {
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

            Log.info("Starting Sync!")
            session.client.startSync()
            Log.info("Matrix bot started.")
        }
    }

    fun onStop(@Observes event: ShutdownEvent) {
        if (!config.enabled()) return

        runBlocking {
            Log.info("Stopping Matrix bot sync...")
            session.client.stopSync()
            Log.info("Matrix bot stopped.")
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
