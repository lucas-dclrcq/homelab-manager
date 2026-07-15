package org.hoohoot.homelab.manager.cleanup.domain

import java.time.LocalDateTime

data class CleanupConfig(
    val diskPath: String,
    val thresholdFreeBytes: Long,
    val targetFreeBytes: Long,
    val graceDays: Long,
    val suggestionGraceDays: Long,
    val minAgeDays: Long,
    val recentSeriesWatchDays: Long,
    val inProgressDays: Long,
    val maxCandidates: Int,
    val minScore: Double,
    val scoring: ScoringConfig,
)

data class ScoringConfig(
    val fullAgeDays: Long,
    val sizeRefBytes: Long,
    val weightLastWatched: Double,
    val weightDownloadAge: Double,
    val weightSize: Double,
    val weightCompletion: Double,
    val weightRequesterActivity: Double,
)

enum class CampaignTrigger { AUTO, MANUAL }

enum class VetoChannel { WEB, BOT }

data class CleanupMovie(
    val radarrMovieId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val tmdbId: Int?,
    val imdbId: String?,
    val sizeBytes: Long,
    val downloadedAt: LocalDateTime?,
    val requester: String?,
    val hasFile: Boolean,
)

data class CleanupSeries(
    val sonarrSeriesId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val tvdbId: Int?,
    val imdbId: String?,
    val continuing: Boolean,
    val addedAt: LocalDateTime?,
    val requester: String?,
    val seasons: List<CleanupSeason>,
)

data class CleanupSeason(
    val seasonNumber: Int,
    val episodeFileCount: Int,
    val sizeBytes: Long,
    val previousAiring: LocalDateTime?,
)

data class JellyfinLibraryEntry(
    val itemId: String,
    val name: String,
    val productionYear: Int?,
    val type: JellyfinEntryType,
    val tmdbId: String?,
    val imdbId: String?,
    val tvdbId: String?,
)

enum class JellyfinEntryType { MOVIE, SERIES }

data class MovieWatchAggregate(
    val itemId: String,
    val itemName: String,
    val lastWatchedAt: LocalDateTime,
    val completedBySomeone: Boolean,
    val lastInProgressAt: LocalDateTime?,
)

data class SeasonWatchAggregate(
    val seriesId: String?,
    val seriesName: String?,
    val seasonNumber: Int?,
    val lastWatchedAt: LocalDateTime,
    val completedBySomeone: Boolean,
    val lastInProgressAt: LocalDateTime?,
)

enum class Correlation { PROVIDER_ID, TITLE, NONE }

data class CorrelatedWatch(
    val correlation: Correlation,
    val lastWatchedAt: LocalDateTime?,
    val completedBySomeone: Boolean,
    val startedBySomeone: Boolean,
    val lastInProgressAt: LocalDateTime?,
)

data class RequesterProfile(
    val username: String,
    val activeMember: Boolean?,
    val lastActivityAt: LocalDateTime?,
)

data class ScoringInput(
    val sizeBytes: Long,
    val downloadedAt: LocalDateTime?,
    val watch: CorrelatedWatch,
    val requester: RequesterProfile?,
)

sealed interface Evaluation {
    data class Excluded(val reason: String) : Evaluation
    data class Scored(val breakdown: ScoreBreakdown) : Evaluation
}

data class ScoreBreakdown(
    val total: Double = 0.0,
    val components: List<ScoreComponent> = emptyList(),
    val inputs: ScoreInputs = ScoreInputs(),
)

data class ScoreComponent(
    val key: String = "",
    val label: String = "",
    val weight: Double = 0.0,
    val rawValue: String = "",
    val normalized: Double = 0.0,
    val points: Double = 0.0,
)

data class ScoreInputs(
    val lastWatchedAt: LocalDateTime? = null,
    val downloadedAt: LocalDateTime? = null,
    val sizeBytes: Long = 0,
    val requester: String? = null,
    val correlation: String = Correlation.NONE.name,
)

data class CampaignState(
    val executionSummary: ExecutionSummary? = null,
)

data class ExecutionSummary(
    val deletedCount: Int = 0,
    val protectedCount: Int = 0,
    val skippedCount: Int = 0,
    val failedCount: Int = 0,
    val freedBytes: Long = 0,
    val finishedAt: LocalDateTime? = null,
    val note: String? = null,
)

data class ActiveProblemIds(
    val radarrMovieIds: Set<Int>,
    val sonarrSeriesIds: Set<Int>,
)

sealed interface DeleteOutcome {
    data class Deleted(val freedBytes: Long) : DeleteOutcome
    data object AlreadyGone : DeleteOutcome
    data class Failed(val reason: String) : DeleteOutcome
}

sealed interface Accessor {
    data class User(val username: String) : Accessor
    data object Admin : Accessor
}
