package org.hoohoot.homelab.manager.operator

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault

@ConfigMapping(prefix = "homelab-operator")
interface OperatorConfig {
    /** Noms des gateways dont les routes nécessitent le VPN */
    @WithDefault("internal")
    fun vpnGateways(): Set<String>

    @WithDefault("homelab-manager.hoohoot.org")
    fun annotationPrefix(): String

    @WithDefault("Uncategorized")
    fun defaultCategory(): String

    /** Intervalle du sweep complet (consommé par @Scheduled) */
    @WithDefault("5m")
    fun syncInterval(): String

    @WithDefault("true")
    fun syncOnStart(): Boolean
}
