package org.hoohoot.homelab.manager.notifications.matrix

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MatrixRoomProvider(config: MatrixConfiguration) {
    @Volatile var media: String = config.room().media()
    @Volatile var music: String = config.room().music()
    @Volatile var support: String = config.room().support()
}
