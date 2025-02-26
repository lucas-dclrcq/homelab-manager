package org.hoohoot.homelab.manager.notifications.matrix

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "matrix.room")
interface MatrixRoomsConfiguration {
    fun sonarr(): String?
    fun radarr(): String?
    fun lidarr(): String?
}
