package org.hoohoot.homelab.manager.notifications.arr.bazarr

import com.fasterxml.jackson.annotation.JsonProperty

// Actions Bazarr considérées comme un téléchargement de sous-titres
object BazarrActions {
    const val DOWNLOADED = 1
    const val UPGRADED = 3
    const val MANUALLY_DOWNLOADED = 4

    val DOWNLOAD_ACTIONS = setOf(DOWNLOADED, UPGRADED, MANUALLY_DOWNLOADED)
}

data class BazarrHistoryPage(
    val data: List<BazarrHistoryItem> = emptyList()
)

data class BazarrHistoryItem(
    val seriesTitle: String? = null,
    val episodeTitle: String? = null,
    val title: String? = null,
    @param:JsonProperty("episode_number")
    val episodeNumber: String? = null,
    val language: BazarrLanguage? = null,
    val provider: String? = null,
    val action: Int? = null,
    val timestamp: String? = null,
    @param:JsonProperty("raw_timestamp")
    val rawTimestamp: String? = null,
    val sonarrEpisodeId: Int? = null,
    val radarrId: Int? = null
)

data class BazarrLanguage(
    val name: String? = null,
    val code2: String? = null
)
