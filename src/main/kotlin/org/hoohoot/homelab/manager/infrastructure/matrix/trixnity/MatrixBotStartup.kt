package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity

import com.trendyol.kediatr.Mediator
import io.ktor.http.*
import io.quarkus.logging.Log
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.login
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import org.fuchss.matrix.bots.MatrixBot
import org.fuchss.matrix.bots.command.HelpCommand
import org.fuchss.matrix.bots.helper.createMediaStore
import org.fuchss.matrix.bots.helper.createRepositoriesModule
import org.fuchss.matrix.bots.helper.handleCommand
import org.fuchss.matrix.bots.helper.handleEncryptedCommand
import org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands.PingCommand
import org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands.WhoWatchedCommand
import java.io.File

@ApplicationScoped
class MatrixBotStartup(private val config: MatrixBotConfiguration, private val mediator: Mediator) {
    fun onStart(@Observes event: StartupEvent) = runBlocking {
        if (config.enabled().not()) return@runBlocking

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            Log.info("Starting Matrix bot...")
            val config = TrixnityConfig.from(config)

            val ping = PingCommand(mediator)
            val whoWatched = WhoWatchedCommand(mediator)
            val help = HelpCommand(config, "Johnny Bot") { listOf(ping, whoWatched) }

            val commands = listOf(help, ping, whoWatched)

            val matrixClient = getMatrixClient(config)

            val matrixBot = MatrixBot(matrixClient, config)
            matrixBot.subscribeContent { event -> handleCommand(commands, event, matrixBot, config) }

            matrixBot.subscribeContent { encryptedEvent ->
                handleEncryptedCommand(
                    commands,
                    encryptedEvent,
                    matrixBot,
                    config
                )
            }

            matrixBot.startBlocking()
        }
    }

    private suspend fun getMatrixClient(config: TrixnityConfig): MatrixClient {
        val existingMatrixClient =
            MatrixClient.fromStore(createRepositoriesModule(config), createMediaStore(config)).getOrThrow()
        if (existingMatrixClient != null) {
            return existingMatrixClient
        }

        val matrixClient = MatrixClient.login(
            baseUrl = Url(config.baseUrl),
            identifier = IdentifierType.User(config.username),
            password = config.password,
            repositoriesModule = createRepositoriesModule(config),
            mediaStore = createMediaStore(config),
            initialDeviceDisplayName = "An interesting bot",
        ).getOrThrow()

        return matrixClient
    }
}

