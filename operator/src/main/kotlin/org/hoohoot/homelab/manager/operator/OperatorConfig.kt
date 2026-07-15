package org.hoohoot.homelab.manager.operator

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "homelab-operator")
interface OperatorConfig {
    @WithDefault("internal")
    fun vpnGateways(): Set<String>

    @WithDefault("homelab-manager.hoohoot.org")
    fun annotationPrefix(): String

    @WithDefault("Uncategorized")
    fun defaultCategory(): String

    @WithDefault("5m")
    fun syncInterval(): String

    @WithDefault("true")
    fun syncOnStart(): Boolean
}
