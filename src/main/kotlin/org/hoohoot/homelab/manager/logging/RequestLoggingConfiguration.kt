package org.hoohoot.homelab.manager.logging

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "api.logging", namingStrategy = ConfigMapping.NamingStrategy.KEBAB_CASE)
interface RequestLoggingConfiguration {
    @WithDefault("true")
    fun logBody(): Boolean
}
