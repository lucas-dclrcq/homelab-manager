package org.hoohoot.homelab.manager.application.ports

enum class JellystatMediaType { Audio, Movies, Series }

data class UniqueViewerStatistics(val uniqueViewers: Int, val name: String)

interface JellystatGateway {
    suspend fun getMostPopularByType(numberOfDays: Int, type: JellystatMediaType): List<UniqueViewerStatistics>
}