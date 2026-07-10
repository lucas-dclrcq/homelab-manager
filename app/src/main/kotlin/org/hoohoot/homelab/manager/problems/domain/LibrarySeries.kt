package org.hoohoot.homelab.manager.problems.domain

data class LibrarySeries(
    val sonarrSeriesId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val overview: String?,
)
