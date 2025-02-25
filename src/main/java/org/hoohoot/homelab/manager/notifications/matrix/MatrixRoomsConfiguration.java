package org.hoohoot.homelab.manager.notifications.matrix;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "matrix.room")
public interface MatrixRoomsConfiguration {
    String sonarr();
    String radarr();
    String lidarr();
}
