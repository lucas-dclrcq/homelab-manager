package org.hoohoot.homelab.manager.problems.domain

data class LibraryMovie(
    val radarrMovieId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val overview: String?,
    val currentQuality: String?,
    val currentLanguages: List<String> = emptyList(),
)
