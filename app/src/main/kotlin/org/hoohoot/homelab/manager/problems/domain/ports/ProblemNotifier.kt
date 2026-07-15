package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.notifications.domain.NotificationId

interface ProblemNotifier {
    suspend fun workflowCreated(username: String, mediaType: String): NotificationId?

    suspend fun problemReported(
        mediaTitle: String?,
        problemType: String?,
        description: String?,
        reporter: String,
        inThread: NotificationId?,
    )

    suspend fun problemResolved(
        mediaTitle: String?,
        reporter: String,
        resolvedBy: String,
        isSelfResolve: Boolean,
        inThread: NotificationId?,
    )
}
