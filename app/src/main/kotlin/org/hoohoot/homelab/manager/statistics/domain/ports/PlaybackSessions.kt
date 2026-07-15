package org.hoohoot.homelab.manager.statistics.domain.ports

import org.hoohoot.homelab.manager.statistics.domain.PlaybackSessionRecord

interface PlaybackSessions {
    suspend fun saveAll(records: List<PlaybackSessionRecord>)

    suspend fun insertIgnoringDuplicates(records: List<PlaybackSessionRecord>): Int
}
