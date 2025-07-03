package org.hoohoot.homelab.manager.infrastructure.matrix.bot

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import net.folivo.trixnity.core.model.UserId

@ConfigMapping(prefix = "matrix.bot")
interface MatrixBotConfiguration {
    fun enabled(): Boolean
    fun prefix(): String
    fun baseUrl(): String
    fun username(): String
    fun password(): String
    fun dataDirectory(): String
    fun admins(): List<String>
    fun users(): List<String>
}