package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.problems.domain.Release

interface Releases {
    suspend fun searchForMovie(radarrMovieId: Int): List<Release>

    /** Lance le téléchargement d'une release ; jette si Radarr refuse. */
    suspend fun grab(guid: String, indexerId: Int)
}
