package org.hoohoot.homelab.manager.infrastructure.matrix.bot

import io.quarkus.logging.Log
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.room.message.react
import net.folivo.trixnity.client.store.repository.exposed.createExposedRepositoriesModule
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.ClientEvent
import net.folivo.trixnity.core.model.events.idOrNull
import net.folivo.trixnity.core.model.events.m.room.EncryptedMessageEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent.TextBased.Text
import net.folivo.trixnity.core.model.events.roomIdOrNull
import net.folivo.trixnity.core.model.events.senderOrNull
import okio.Path.Companion.toOkioPath
import org.jetbrains.exposed.sql.Database
import java.io.File


suspend fun createRepositoriesModule(config: MatrixBotConfiguration) =
    createExposedRepositoriesModule(database = Database.connect("jdbc:h2:${config.dataDirectory()}/database;DB_CLOSE_DELAY=-1"))

fun createMediaStore(config: MatrixBotConfiguration) =
    OkioMediaStore(File(config.dataDirectory() + "/media").toOkioPath())

suspend fun decryptMessage(
    event: ClientEvent<EncryptedMessageEventContent>,
    matrixBot: MatrixBot,
    handler: suspend (EventId, UserId, RoomId, Text) -> Unit
) {
    val eventId = event.idOrNull ?: return
    val roomId = event.roomIdOrNull ?: return
    val sender = event.senderOrNull ?: return

    Log.debugf("Waiting for decryption of {} ..", event)
    val decryptedEvent = matrixBot.room().getTimelineEvent(roomId, eventId).firstWithTimeout { it?.content != null }
    if (decryptedEvent != null) {
        Log.debugf("Decryption of {} was successful", event)
    }

    if (decryptedEvent == null) {
        Log.error("Cannot decrypt event $event within the given time ..")
        return
    }

    val content = decryptedEvent.content?.getOrNull() ?: return
    if (content is Text) {
        handler(eventId, sender, roomId, content)
    }
}

suspend fun handleEncryptedCommand(
    commands: List<MatrixBotCommand>,
    event: ClientEvent<EncryptedMessageEventContent>,
    matrixBot: MatrixBot,
    config: MatrixBotConfiguration,
    defaultCommand: String? = null
) {
    decryptMessage(event, matrixBot) { eventId, sender, roomId, text ->
        executeCommand(commands, sender, matrixBot, roomId, eventId, text, config, defaultCommand)
    }
}

suspend fun handleCommand(
    commands: List<MatrixBotCommand>,
    event: ClientEvent<RoomMessageEventContent>,
    matrixBot: MatrixBot,
    config: MatrixBotConfiguration,
    defaultCommand: String? = null
) {
    val roomId = event.roomIdOrNull ?: return
    val sender = event.senderOrNull ?: return
    val eventId = event.idOrNull ?: return
    val content = event.content
    if (content is Text) {
        executeCommand(commands, sender, matrixBot, roomId, eventId, content, config, defaultCommand)
    }
}

private suspend fun executeCommand(
    commands: List<MatrixBotCommand>,
    sender: UserId,
    matrixBot: MatrixBot,
    roomId: RoomId,
    textEventId: EventId,
    textEvent: Text,
    config: MatrixBotConfiguration,
    defaultCommand: String?
) {
    var message = textEvent.body
    if (!message.startsWith("!${config.prefix()}")) {
        return
    }
    message = message.substring("!${config.prefix()}".length).trim()

    val command = message.split(Regex(" "), 2)[0]
    var parameters = message.substring(command.length).trim()

    var commandToExecute = commands.find { it.name == command }
    if (commandToExecute == null && defaultCommand != null) {
        commandToExecute = commands.find { it.name == defaultCommand }
        parameters = message
    }

    if (commandToExecute == null) {
        return
    }

    if (commandToExecute.autoAcknowledge) {
        matrixBot.room().sendMessage(roomId) {
            react(textEventId, MatrixBotCommand.ACK_EMOJI)
        }
    }

    commandToExecute.execute(matrixBot, sender, roomId, parameters, textEventId, textEvent)
}