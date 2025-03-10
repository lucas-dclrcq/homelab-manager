package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.faulttolerance.CircuitBreaker
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
    @CircuitBreaker(requestVolumeThreshold = 4)
    override suspend fun sendMediaNotification(notification: Notification): NotificationId =
        this.sendNotification(notification, matrixRooms.media())

    @CircuitBreaker(requestVolumeThreshold = 4)
    override suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId?): NotificationId =
        this.sendNotification(notification, matrixRooms.support(), relatedTo)

    @CircuitBreaker(requestVolumeThreshold = 4)
    override suspend fun sendMusicNotification(notification: Notification): NotificationId =
        this.sendNotification(notification, matrixRooms.music())

    private suspend fun sendNotification(notification: Notification, roomId: String, relatedNotificationId: NotificationId? = null): NotificationId = matrixRestClient.sendMessage(
        roomId,
        UUID.randomUUID().toString(),
        MatrixMessage.html(notification.textMessage, notification.htmlMessage, relatedNotificationId)
    ).let { NotificationId(it.eventId) }
}