package org.hoohoot.homelab.manager.corrector.domain.ports

import org.hoohoot.homelab.manager.corrector.domain.LibraryMovie

interface MovieLibrary {
    suspend fun allMovies(): List<LibraryMovie>
}
