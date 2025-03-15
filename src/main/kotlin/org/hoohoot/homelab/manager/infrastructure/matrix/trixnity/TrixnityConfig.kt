package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity

import org.fuchss.matrix.bots.IConfig

class TrixnityConfig(
    override val users: List<String>,
    override val admins: List<String>,
    override val dataDirectory: String,
    override val password: String,
    override val username: String,
    override val prefix: String,
    override val baseUrl: String
) : IConfig {
    companion object {
        fun from(config: MatrixBotConfiguration): TrixnityConfig {
            val trixnityConfig = TrixnityConfig(
                config.users(),
                config.admins(),
                config.dataDirectory(),
                config.password(),
                config.username(),
                config.prefix(),
                config.baseUrl()
            )
            trixnityConfig.validate()
            return trixnityConfig
        }
    }
}