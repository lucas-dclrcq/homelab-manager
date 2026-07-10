package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.NowPlayingSession
import org.hoohoot.homelab.manager.statistics.domain.ports.NowPlayingSource

@ApplicationScoped
class GetNowPlaying(private val source: NowPlayingSource) {
    operator fun invoke(): List<NowPlayingSession> = source.current()
}
