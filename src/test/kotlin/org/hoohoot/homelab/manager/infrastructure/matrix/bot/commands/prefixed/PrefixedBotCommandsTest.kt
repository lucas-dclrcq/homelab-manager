package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.prefixed

import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent.TextBased.Text
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.PrefixedBotCommand
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PrefixedBotCommandsTest {

    @Test
    fun `should include HelpCommand and return help output`() {
        // Dummy command for testing
        class DummyCommand : PrefixedBotCommand() {
            override val name = "dummy"
            override val help = "dummy help"
            override suspend fun handle(
                matrixBot: MatrixBot,
                sender: UserId,
                roomId: RoomId,
                parameters: String,
                textEventId: EventId,
                textEvent: Text
            ) {
            }
        }

        val config =
            object :
                org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBotConfiguration {
                override fun enabled(): Boolean = true
                override fun prefix(): String = "!"
                override fun baseUrl(): String = "https://matrix.example.com"
                override fun username(): String = "testuser"
                override fun password(): String = "testpass"
                override fun dataDirectory(): String = "/tmp"
                override fun admins(): List<String> = listOf("admin1")
                override fun users(): List<String> = listOf("user1", "user2")
            }

        val commands = mutableListOf<PrefixedBotCommand>(DummyCommand())
        val prefixedBotCommands = PrefixedBotCommands(commands, config)

        val helpCommand = prefixedBotCommands.find("help")
        assertNotNull(helpCommand)
        assertEquals("help", helpCommand?.name)
        assertEquals("shows this help message", helpCommand?.help)

        val dummyCommand = prefixedBotCommands.find("dummy")
        assertNotNull(dummyCommand)
    }
}
