package org.hoohoot.homelab.manager.notifications.domain.ports

import org.hoohoot.homelab.manager.notifications.domain.MostPopularMedia

interface ViewingStats {
    suspend fun topMovies(lastDays: Int, limit: Int): List<MostPopularMedia>
    suspend fun topSeries(lastDays: Int, limit: Int): List<MostPopularMedia>
}
