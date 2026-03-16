package org.hoohoot.homelab.manager.matrix.bot

import com.vdurmont.emoji.EmojiManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import net.folivo.trixnity.client.room.message.MessageBuilder
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val MATRIX_TO_PREFIX = "https://matrix.to/#/"
private val ROOM_ID_REGEX = Regex("^![a-zA-Z0-9]+:[a-zA-Z0-9.]+\$")
private val ROOM_ALIAS_REGEX = Regex("^#[a-zA-Z0-9_-]+:[a-zA-Z0-9._-]+\$")

suspend fun <T> Flow<T>.firstWithTimeout(
    timeout: Duration = 3000.milliseconds,
    predicate: suspend (T) -> Boolean
): T? {
    val that = this
    return withTimeoutOrNull(timeout) { that.first { predicate(it) } }
}

fun String.emoji(): String = EmojiManager.getForAlias(this).unicode

fun MessageBuilder.markdown(markdown: String) {
    val document = Parser.builder().build().parse(markdown)
    val html = HtmlRenderer.builder().build().render(document)
    text(markdown, format = "org.matrix.custom.html", formattedBody = html)
}

fun RoomId.matrixTo(): String = "$MATRIX_TO_PREFIX${this.full}?via=${this.domain}"

fun UserId.matrixTo(): String = "${MATRIX_TO_PREFIX}${this.full}"

fun String.syntaxOfRoomId(): Boolean {
    var cleanedInput = this.trim()
    if (cleanedInput.startsWith(MATRIX_TO_PREFIX)) {
        cleanedInput = cleanedInput.removePrefix(MATRIX_TO_PREFIX)
        cleanedInput = cleanedInput.substringBefore("?")
    }
    return cleanedInput.matches(ROOM_ID_REGEX) || cleanedInput.matches(ROOM_ALIAS_REGEX)
}

suspend fun String.toInternalRoomIdOrNull(matrixBot: MatrixBot): RoomId? {
    var cleanedInput = this.trim()
    if (cleanedInput.startsWith(MATRIX_TO_PREFIX)) {
        cleanedInput = cleanedInput.removePrefix(MATRIX_TO_PREFIX)
        cleanedInput = cleanedInput.substringBefore("?")
    }

    if (cleanedInput.startsWith("#")) {
        return matrixBot.resolvePublicRoomIdOrNull(cleanedInput)
    }

    if (cleanedInput.matches(ROOM_ID_REGEX)) {
        return RoomId(cleanedInput)
    }
    return null
}

fun MatrixBotConfiguration.isUser(user: UserId?): Boolean {
    if (user == null) {
        return false
    }
    if (users().isEmpty()) {
        return true
    }
    return users().any { user.full.endsWith(it) }
}
