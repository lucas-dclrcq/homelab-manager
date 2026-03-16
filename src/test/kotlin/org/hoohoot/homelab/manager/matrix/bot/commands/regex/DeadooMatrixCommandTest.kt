package org.hoohoot.homelab.manager.matrix.bot.commands.regex

import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.regex.DeadooMatrixCommand
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DeadooMatrixCommandTest {
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
        assertThat(DeadooMatrixCommand().matches(input)).isTrue()
    }
}
