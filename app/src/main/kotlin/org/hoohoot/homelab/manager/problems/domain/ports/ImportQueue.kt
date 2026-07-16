package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.problems.domain.BlockedImport

interface ImportQueue {
    suspend fun blockedImports(): List<BlockedImport>
    suspend fun forceImport(downloadId: String, radarrMovieId: Int): Boolean
}
