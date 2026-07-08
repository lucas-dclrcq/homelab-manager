package org.hoohoot.homelab.manager.corrector.domain.ports

import org.hoohoot.homelab.manager.corrector.domain.Release

interface Releases {
    suspend fun searchForMovie(radarrMovieId: Int): List<Release>

    /** Lance le téléchargement d'une release ; jette si Radarr refuse. */
    suspend fun grab(guid: String, indexerId: Int)
}
