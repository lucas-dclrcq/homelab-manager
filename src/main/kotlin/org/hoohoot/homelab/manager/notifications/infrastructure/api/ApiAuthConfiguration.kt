package org.hoohoot.homelab.manager.notifications.infrastructure.api

import io.smallrye.config.ConfigMapping
import io.smallrye.config.ConfigMapping.NamingStrategy
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "api.security", namingStrategy = NamingStrategy.KEBAB_CASE)
interface ApiAuthConfiguration {
    @WithDefault("false")
    fun enabled(): Boolean
    fun apiKey(): String?
    fun endpoints(): String?
}
