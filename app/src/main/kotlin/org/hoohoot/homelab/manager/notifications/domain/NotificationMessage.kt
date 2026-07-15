package org.hoohoot.homelab.manager.notifications.domain

data class NotificationMessage(
    val body: String,
    val formattedBody: String,
)

fun notificationMessage(header: String, lines: List<String>) = NotificationMessage(
    body = (listOf(header) + lines).joinToString("\n"),
    formattedBody = "<h1>$header</h1><p>${lines.joinToString("<br>")}</p>",
)
