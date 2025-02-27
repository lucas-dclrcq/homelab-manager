package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.domain.Notification
import org.hoohoot.homelab.manager.notifications.domain.NotificationId
import java.util.*

@ApplicationScoped
class MatrixNotificationGateway(
    @param:RestClient private val matrixRestClient: MatrixRestClient,
    private val matrixRooms: MatrixRoomsConfiguration
) : NotificationGateway {
    override suspend fun sendMovieNotification(notification: Notification): NotificationId = this.sendNotification(notification, matrixRooms.media())

    override suspend fun sendSeriesNotification(notification: Notification): NotificationId = this.sendNotification(notification, matrixRooms.media())

    override suspend fun sendIssueNotification(notification: Notification): NotificationId = this.sendNotification(notification, matrixRooms.support())

    override suspend fun sendMusicNotification(notification: Notification): NotificationId = this.sendNotification(notification, matrixRooms.music())

    private suspend fun sendNotification(notification: Notification, roomId: String): NotificationId = matrixRestClient.sendMessage(
        roomId,
        UUID.randomUUID().toString(),
        MatrixMessage.html(notification.textMessage, notification.htmlMessage)
    ).let { NotificationId(roomId) }
}