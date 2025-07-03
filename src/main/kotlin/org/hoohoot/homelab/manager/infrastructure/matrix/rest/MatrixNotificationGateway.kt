package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.Notification
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationId
import java.util.*

@ApplicationScoped
class MatrixNotificationGateway(
    @param:RestClient private val matrixRestClient: MatrixRestClient,
    private val matrixRooms: MatrixRoomsConfiguration
) : NotificationGateway {
    override suspend fun sendMediaNotification(notification: Notification, relatedTo: NotificationId?): NotificationId =
        this.sendNotification(notification, matrixRooms.media(), relatedTo)

    override suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId?): NotificationId =
        this.sendNotification(notification, matrixRooms.support(), relatedTo)

    override suspend fun sendMusicNotification(notification: Notification): NotificationId =
        this.sendNotification(notification, matrixRooms.music())

    private suspend fun sendNotification(notification: Notification, roomId: String, relatedNotificationId: NotificationId? = null): NotificationId = matrixRestClient.sendMessage(
        roomId,
        UUID.randomUUID().toString(),
        MatrixMessage.html(notification.textMessage, notification.htmlMessage, relatedNotificationId)
    ).let { NotificationId(it.eventId) }
}