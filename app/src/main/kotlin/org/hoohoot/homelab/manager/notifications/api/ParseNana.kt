package org.hoohoot.homelab.manager.notifications.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hoohoot.homelab.manager.notifications.domain.usecases.BookDownload
import java.util.Locale

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanaWebhookPayload(
    val event: String? = null,
    val download: NanaWebhookDownload? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanaWebhookDownload(
    val title: String? = null,
    val extension: String? = null,
    val requestedBy: String? = null,
    val sizeBytes: Long? = null,
    val errorMessage: String? = null,
)

private const val DEFAULT_VALUE = "unknown"

fun NanaWebhookPayload.title(): String = download?.title ?: DEFAULT_VALUE
fun NanaWebhookPayload.extension(): String = download?.extension ?: DEFAULT_VALUE
fun NanaWebhookPayload.requestedBy(): String = download?.requestedBy ?: DEFAULT_VALUE
fun NanaWebhookPayload.errorMessage(): String? = download?.errorMessage

fun NanaWebhookPayload.size(): String = download?.sizeBytes
    ?.let { humanReadableSize(it) }
    ?: DEFAULT_VALUE

fun NanaWebhookPayload.toBookDownload(succeeded: Boolean) = BookDownload(
    title = title(),
    extension = extension(),
    requestedBy = requestedBy(),
    size = size(),
    succeeded = succeeded,
    errorMessage = errorMessage(),
)

private val SIZE_UNITS = listOf("B", "KB", "MB", "GB", "TB")

private fun humanReadableSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < SIZE_UNITS.size - 1) {
        value /= 1024
        unit++
    }
    return String.format(Locale.US, "%.1f %s", value, SIZE_UNITS[unit])
}
