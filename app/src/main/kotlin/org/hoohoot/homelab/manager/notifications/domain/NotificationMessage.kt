package org.hoohoot.homelab.manager.notifications.domain

data class NotificationMessage(
    val body: String,
    val formattedBody: String,
)

/**
 * Rendu standard d'une notification : le texte brut joint les lignes par des sauts
 * de ligne, la version HTML les joint par des <br> sous un titre <h1>.
 */
fun notificationMessage(header: String, lines: List<String>) = NotificationMessage(
    body = (listOf(header) + lines).joinToString("\n"),
    formattedBody = "<h1>$header</h1><p>${lines.joinToString("<br>")}</p>",
)
