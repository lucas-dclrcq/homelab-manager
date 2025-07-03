package org.hoohoot.homelab.manager.domain.media_notifications

data class NotificationId(val value: String)

data class Notification (val textMessage: String, val htmlMessage: String, val relatedTo: NotificationId? = null)

class EmptyNotificationException : Throwable("Notification cannot be empty. It should have at least a title or an info line")

