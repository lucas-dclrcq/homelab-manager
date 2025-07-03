package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.regex

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandWithResult
import com.trendyol.kediatr.Mediator
import com.trendyol.kediatr.Notification
import com.trendyol.kediatr.PublishStrategy
import com.trendyol.kediatr.Query
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DeadooMatrixCommandTest {
    @ParameterizedTest
    @ValueSource(strings = [
        "C'est comment ?",
        "C'est comment",
        "c'est comment",
        "Et sinon c'est comment ?",
        "c'est comment sinon ?"
    ])
    fun `should match for c'est comment`(input: String) {
        assertThat(DeadooMatrixCommand(FakeMediator()).matches(input)).isTrue()
    }
}

class FakeMediator : Mediator {
    override suspend fun <T : Notification> publish(notification: T) {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Notification> publish(
        notification: T,
        publishStrategy: PublishStrategy
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun <TCommand : Command> send(command: TCommand) {
        TODO("Not yet implemented")
    }

    override suspend fun <TCommand : CommandWithResult<TResult>, TResult> send(command: TCommand): TResult {
        TODO("Not yet implemented")
    }

    override suspend fun <TQuery : Query<TResponse>, TResponse> send(query: TQuery): TResponse {
        TODO("Not yet implemented")
    }
}