package org.hoohoot.homelab.manager.it

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.SynapseTestResource
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed.PrefixedBotCommands
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.regex.DeadooMatrixCommand
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.regex.RegexBotCommands
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@QuarkusTest
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class BotCommandsTest {

    @Inject
    lateinit var prefixedBotCommands: PrefixedBotCommands

    @Inject
    lateinit var regexBotCommands: RegexBotCommands

    @Inject
    lateinit var deadooMatrixCommand: DeadooMatrixCommand

    @Test
    fun `should include HelpCommand in prefixed commands`() {
        val helpCommand = prefixedBotCommands.find("help")
        assertThat(helpCommand).isNotNull
        assertThat(helpCommand?.name).isEqualTo("help")
        assertThat(helpCommand?.help).isEqualTo("shows this help message")
    }

    @Test
    fun `should include ping command in prefixed commands`() {
        val pingCommand = prefixedBotCommands.find("ping")
        assertThat(pingCommand).isNotNull
    }

    @Test
    fun `should return null for unknown prefixed command`() {
        val command = prefixedBotCommands.find("nonexistent")
        assertThat(command).isNull()
    }

    @Test
    fun `should find deadoo regex command`() {
        val command = regexBotCommands.find("c'est comment")
        assertThat(command).isNotNull
        assertThat(command?.name).isEqualTo("deadoo")
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "C'est comment ?",
        "C'est comment",
        "c'est comment",
        "Et sinon c'est comment ?",
        "c'est comment sinon ?",
        "deado",
        "DEADO",
        "deeeeeAAAAaaaaDDDdddOOOooo",
        "dddddeeeeeAAAADDooooDooo",
        "il est deado",
        "il est DDdeEEAAadoOOO",
    ])
    fun `should match for c'est comment or deado`(input: String) {
        assertThat(deadooMatrixCommand.matches(input)).isTrue()
    }

    @Test
    fun `should not match unrelated messages`() {
        assertThat(deadooMatrixCommand.matches("hello world")).isFalse()
        assertThat(deadooMatrixCommand.matches("comment ça va")).isFalse()
    }
}
