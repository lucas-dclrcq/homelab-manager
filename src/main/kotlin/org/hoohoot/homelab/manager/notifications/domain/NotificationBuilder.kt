package org.hoohoot.homelab.manager.notifications.domain

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

    fun addBoldInfoLine(infoLine: String): NotificationBuilder {
        if (textBody.isNotEmpty() && textBody != "\n") textBody += "\n"
        if (htmlBody.isNotEmpty() && htmlBody != "<br>") htmlBody += "<br>"

        textBody += infoLine
        htmlBody += "<b>$infoLine</b>"

        return this
    }

    fun addInfoLine(infoLine: String): NotificationBuilder {
        if (textBody.isNotEmpty() && textBody != "\n") textBody += "\n"
        if (htmlBody.isNotEmpty() && htmlBody != "<br>") htmlBody += "<br>"

        textBody += infoLine
        htmlBody += infoLine

        return this
    }

    fun addEmptyLine(): NotificationBuilder {
        textBody += "\n"
        htmlBody += "<br>"
        return this
    }

    fun addInfoLines(infoLines: List<String>): NotificationBuilder {
        infoLines.forEach { addInfoLine(it) }
        return this
    }
}