package org.hoohoot.homelab.manager.statistics.domain.ports

import org.hoohoot.homelab.manager.statistics.domain.NowPlayingSession

interface NowPlayingSource {
    fun current(): List<NowPlayingSession>
}
