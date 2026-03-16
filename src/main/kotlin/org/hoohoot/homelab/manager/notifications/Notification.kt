package org.hoohoot.homelab.manager.notifications

data class NotificationId(val value: String)

data class Notification(val textMessage: String, val htmlMessage: String)

class EmptyNotificationException : Throwable("Notification cannot be empty. It should have at least a title or an info line")
