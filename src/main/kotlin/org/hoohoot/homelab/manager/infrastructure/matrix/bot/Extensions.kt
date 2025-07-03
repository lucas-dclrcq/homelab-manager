package org.hoohoot.homelab.manager.infrastructure.matrix.bot

import com.vdurmont.emoji.Emoji
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

/**
 * Same as [Flow.first] but with a defined timeout that leads to null if reached.
 * @param predicate a predicate to filter the results of [Flow.first]
 * @return the result of [Flow.first] or null
 */
suspend fun <T> Flow<T>.firstWithTimeout(
    timeout: Duration = 3000.milliseconds,
    predicate: suspend (T) -> Boolean
): T? {
    val that = this
    return withTimeoutOrNull(timeout) { that.first { predicate(it) } }
}

/**
 * Convert a string emoji to an [Emoji].
 */
fun String.emoji(): String = EmojiManager.getForAlias(this).unicode

/**
 * Format a markdown message and send it using a [MessageBuilder]
 * @param[markdown] the plain Markdown text
 */
fun MessageBuilder.markdown(markdown: String) {
    val document = Parser.builder().build().parse(markdown)
    val html = HtmlRenderer.builder().build().render(document)
    text(markdown, format = "org.matrix.custom.html", formattedBody = html)
}

/**
 * Create a matrix.to link for a RoomId
 * @return the matrix.to link
 */
fun RoomId.matrixTo(): String = "$MATRIX_TO_PREFIX${this.full}?via=${this.domain}"

/**
 * Create a matrix.to link for a UserId
 * @return the matrix.to link
 */
fun UserId.matrixTo(): String = "${MATRIX_TO_PREFIX}${this.full}"

/**
 * Indicates if a string is a valid RoomId (syntax)
 */
fun String.syntaxOfRoomId(): Boolean {
    var cleanedInput = this.trim()
    if (cleanedInput.startsWith(MATRIX_TO_PREFIX)) {
        cleanedInput = cleanedInput.removePrefix(MATRIX_TO_PREFIX)
        cleanedInput = cleanedInput.substringBefore("?")
    }
    return cleanedInput.matches(Regex("^![a-zA-Z0-9]+:[a-zA-Z0-9.]+\$")) || cleanedInput.matches(Regex("^#[a-zA-Z0-9_-]+:[a-zA-Z0-9._-]+\$"))
}

/**
 * Extract a RoomId from a string. The string can be a matrix.to link or a room id.
 * @return the RoomId or null if the string is not a valid room id
 */
suspend fun String.toInternalRoomIdOrNull(matrixBot: MatrixBot): RoomId? {
    var cleanedInput = this.trim()
    if (cleanedInput.startsWith(MATRIX_TO_PREFIX)) {
        cleanedInput = cleanedInput.removePrefix(MATRIX_TO_PREFIX)
        cleanedInput = cleanedInput.substringBefore("?")
    }

    if (cleanedInput.startsWith("#")) {
        // Alias RoomId
        return matrixBot.resolvePublicRoomIdOrNull(cleanedInput)
    }

    if (cleanedInput.matches(Regex("^![a-zA-Z0-9]+:[a-zA-Z0-9.]+\$"))) {
        return RoomId(cleanedInput)
    }
    return null
}



/**
 * Determine whether a user id belongs to an authorized user.
 * @param[user] the user id to check
 * @return indicator for authorization
 */
fun MatrixBotConfiguration.isUser(user: UserId?): Boolean {
    if (user == null) {
        return false
    }
    if (users().isEmpty()) {
        return true
    }
    return users().any { user.full.endsWith(it) }
}