package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.regex

import io.quarkus.arc.All
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.RegexBotCommand

@ApplicationScoped
class RegexBotCommands(@param:All private val commands: MutableList<RegexBotCommand>) {
    fun commands(): List<RegexBotCommand> = commands

    fun find(message: String): RegexBotCommand? = commands.find { it.regex.matches(message) }
}
