package org.hoohoot.homelab.manager.statistics.domain.ports

import org.hoohoot.homelab.manager.statistics.domain.PlaybackSessionRecord

interface PlaybackSessions {
    suspend fun saveAll(records: List<PlaybackSessionRecord>)

    /** Insère en ignorant les doublons (dédup sur import_key) et retourne le nombre de lignes réellement insérées. */
    suspend fun insertIgnoringDuplicates(records: List<PlaybackSessionRecord>): Int
}
