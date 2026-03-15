package org.hoohoot.homelab.manager.matrix

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "matrix")
interface MatrixConfiguration {
    fun baseUrl(): String
    fun accessToken(): String
    fun room(): MatrixRoomsConfiguration
}
