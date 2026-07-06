package org.hoohoot.homelab.manager.notifications.arr

data class DiskSpace(
    val path: String? = null,
    val label: String? = null,
    val freeSpace: Long? = null,
    val totalSpace: Long? = null,
)
