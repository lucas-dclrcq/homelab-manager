package org.hoohoot.homelab.manager.notifications.domain

data class NotificationId(val value: String)

data class Notification (val textMessage: String, val htmlMessage: String, val relatedTo: NotificationId? = null)

class EmptyNotificationException : Throwable("Notification cannot be empty. It should have at least a title or an info line")

class NotificationBuilder {
    private var htmlTitle = ""
    private var textTitle = ""

    private var textBody = ""
    private var htmlBody = ""

    fun buildNotification(): Notification {
        if (textTitle.isEmpty() && htmlTitle.isEmpty()) throw EmptyNotificationException()

        val text = if (textBody.isEmpty()) textTitle else "$textTitle\n$textBody"
        val html = if (htmlBody.isEmpty()) htmlTitle else "$htmlTitle<p>$htmlBody</p>"

        return Notification(text, html)
    }

    fun addTitle(title: String): NotificationBuilder {
        htmlTitle += "<h1>$title</h1>"
        textTitle += title

        return this
    }

    fun addInfoLine(infoLine: String): NotificationBuilder {
        if (textBody.isNotEmpty()) textBody += "\n"
        if (htmlBody.isNotEmpty()) htmlBody += "<br>"

        textBody += infoLine
        htmlBody += infoLine

        return this
    }
}