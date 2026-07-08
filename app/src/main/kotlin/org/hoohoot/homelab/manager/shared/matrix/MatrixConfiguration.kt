package org.hoohoot.homelab.manager.shared.matrix

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "matrix")
interface MatrixConfiguration {
    fun baseUrl(): String
    fun accessToken(): String
    fun room(): MatrixRoomsConfiguration
}
