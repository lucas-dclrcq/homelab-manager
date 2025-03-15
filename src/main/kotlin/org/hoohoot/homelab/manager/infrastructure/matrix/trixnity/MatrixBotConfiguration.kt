package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "matrix.bot")
interface MatrixBotConfiguration  {
    fun enabled(): Boolean
    fun prefix(): String
    fun baseUrl(): String
    fun username(): String
    fun password(): String
    fun dataDirectory(): String
    fun admins(): List<String>
    fun users(): List<String>

}
