package org.hoohoot.homelab.manager.leader

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithDefault
import java.util.Optional

@ConfigMapping(prefix = "leader-election")
interface LeaderElectionConfiguration {
    fun instanceId(): Optional<String>
}
