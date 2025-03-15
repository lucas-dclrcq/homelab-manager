package org.hoohoot.homelab.manager.infrastructure.api.security

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "api.security", namingStrategy = ConfigMapping.NamingStrategy.KEBAB_CASE)
interface ApiAuthConfiguration {
    @WithDefault("false")
    fun enabled(): Boolean
    fun apiKey(): String?
    fun endpoints(): String?
}