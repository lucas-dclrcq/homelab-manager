package org.hoohoot.homelab.manager.cleanup.domain.ports

import org.hoohoot.homelab.manager.cleanup.domain.CleanupMovie
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeries
import org.hoohoot.homelab.manager.cleanup.domain.DeleteOutcome
import org.hoohoot.homelab.manager.cleanup.domain.JellyfinLibraryEntry
import java.time.LocalDateTime

interface MovieCatalog {
    suspend fun allMovies(): List<CleanupMovie>
}

interface SeriesCatalog {
    suspend fun allSeries(): List<CleanupSeries>

    // Date du dernier episodefile importé par saison — un appel Sonarr par série, à réserver
    // aux séries déjà pré-filtrées
    suspend fun seasonDownloadDates(sonarrSeriesId: Int): Map<Int, LocalDateTime>
}

interface MovieEraser {
    // sizeBytes : taille connue au scan, reprise dans Deleted (le DELETE Radarr ne renvoie rien)
    suspend fun deleteMovie(radarrMovieId: Int, sizeBytes: Long): DeleteOutcome
}

interface SeasonEraser {
    suspend fun deleteSeason(sonarrSeriesId: Int, seasonNumber: Int): DeleteOutcome
}

interface JellyfinCatalog {
    suspend fun libraryEntries(): List<JellyfinLibraryEntry>
}
