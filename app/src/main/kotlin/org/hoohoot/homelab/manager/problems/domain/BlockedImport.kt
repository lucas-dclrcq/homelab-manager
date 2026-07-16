package org.hoohoot.homelab.manager.problems.domain

/** Item de la queue Radarr dont l'import est bloqué (trackedDownloadState importBlocked/importPending). */
data class BlockedImport(
    val radarrMovieId: Int,
    val downloadId: String,
    val title: String?,
    val statusMessages: List<String>,
)

private val FORCEABLE_REJECTIONS = listOf(
    "not an upgrade",
    "not a quality revision upgrade",
    "not a custom format upgrade",
)

fun isForceableRejection(message: String): Boolean =
    FORCEABLE_REJECTIONS.any { message.contains(it, ignoreCase = true) }
