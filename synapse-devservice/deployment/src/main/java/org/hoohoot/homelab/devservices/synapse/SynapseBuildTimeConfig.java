package org.hoohoot.homelab.devservices.synapse;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.synapse.devservices")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface SynapseBuildTimeConfig {

    @WithDefault("true")
    boolean enabled();

    @WithDefault("matrixdotorg/synapse:v1.150.0")
    String synapseImage();

    @WithDefault("vectorim/element-web:latest")
    String elementImage();
}
