package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.prefixed

import io.quarkus.arc.All
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBotConfiguration
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class PrefixedBotCommands(@All private val commands: MutableList<PrefixedBotCommand>, private val config: MatrixBotConfiguration) {
    fun commands(): List<PrefixedBotCommand> {
        val help = HelpCommand(config, "Johnny Bot") { commands }
        return listOf(help) + commands
    }

    fun find(command: String): PrefixedBotCommand? = commands.find { it.name == command }
}