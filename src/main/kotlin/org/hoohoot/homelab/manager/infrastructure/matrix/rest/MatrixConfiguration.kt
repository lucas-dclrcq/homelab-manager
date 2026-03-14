package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "matrix")
interface MatrixConfiguration {
    fun baseUrl(): String
    fun accessToken(): String
    fun room(): MatrixRoomsConfiguration
}
