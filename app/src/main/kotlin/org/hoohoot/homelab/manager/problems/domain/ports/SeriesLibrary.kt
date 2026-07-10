package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.problems.domain.LibrarySeries

interface SeriesLibrary {
    suspend fun allSeries(): List<LibrarySeries>
}
