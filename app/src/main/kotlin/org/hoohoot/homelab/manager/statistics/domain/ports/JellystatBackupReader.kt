package org.hoohoot.homelab.manager.statistics.domain.ports

import org.hoohoot.homelab.manager.statistics.domain.PlaybackSessionRecord
import java.nio.file.Path

data class JellystatBackupContent(val records: List<PlaybackSessionRecord>, val ignored: Int)

interface JellystatBackupReader {
    fun read(file: Path): JellystatBackupContent
}
