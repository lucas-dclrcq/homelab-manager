package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.problems.domain.LibraryMovie

interface MovieLibrary {
    suspend fun allMovies(): List<LibraryMovie>
}
