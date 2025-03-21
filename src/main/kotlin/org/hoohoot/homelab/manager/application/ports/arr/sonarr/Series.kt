package org.hoohoot.homelab.manager.application.ports.arr.sonarr

import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Image
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Language
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Rating
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Season

data class Series(
    val title: String? = null,
    val sortTitle: String? = null,
    val status: String? = null,
    val ended: Boolean? = null,
    val overview: String? = null,
    val network: String? = null,
    val airTime: String? = null,
    val images: List<Image>? = null,
    val originalLanguage: Language? = null,
    val seasons: List<Season>? = null,
    val year: Int? = null,
    val path: String? = null,
    val qualityProfileId: Int? = null,
    val seasonFolder: Boolean? = null,
    val monitored: Boolean? = null,
    val monitorNewItems: String? = null,
    val useSceneNumbering: Boolean? = null,
    val runtime: Int? = null,
    val tvdbId: Int? = null,
    val tvRageId: Int? = null,
    val tvMazeId: Int? = null,
    val tmdbId: Int? = null,
    val firstAired: String? = null,
    val lastAired: String? = null,
    val seriesType: String? = null,
    val cleanTitle: String? = null,
    val imdbId: String? = null,
    val titleSlug: String? = null,
    val genres: List<String>? = null,
    val tags: List<Int>? = null,
    val added: String? = null,
    val ratings: Rating? = null,
    val languageProfileId: Int? = null,
    val id: Int? = null
)