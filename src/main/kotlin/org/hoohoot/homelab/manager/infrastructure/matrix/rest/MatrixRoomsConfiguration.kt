package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "matrix.room")
interface MatrixRoomsConfiguration {
    fun media(): String
    fun music(): String
    fun support(): String
}
