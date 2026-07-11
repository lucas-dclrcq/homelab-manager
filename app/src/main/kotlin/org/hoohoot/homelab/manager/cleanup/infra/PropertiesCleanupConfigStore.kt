package org.hoohoot.homelab.manager.cleanup.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.domain.ScoringConfig
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupConfigStore

@ApplicationScoped
class PropertiesCleanupConfigStore(
    @param:ConfigProperty(name = "cleanup.disk-path") private val diskPath: String,
    @param:ConfigProperty(name = "cleanup.threshold-free-gb") private val thresholdFreeGb: Long,
    @param:ConfigProperty(name = "cleanup.target-free-gb") private val targetFreeGb: Long,
    @param:ConfigProperty(name = "cleanup.grace-days") private val graceDays: Long,
    @param:ConfigProperty(name = "cleanup.min-age-days") private val minAgeDays: Long,
    @param:ConfigProperty(name = "cleanup.recent-series-watch-days") private val recentSeriesWatchDays: Long,
    @param:ConfigProperty(name = "cleanup.in-progress-days") private val inProgressDays: Long,
    @param:ConfigProperty(name = "cleanup.max-candidates") private val maxCandidates: Int,
    @param:ConfigProperty(name = "cleanup.min-score") private val minScore: Double,
    @param:ConfigProperty(name = "cleanup.score.full-age-days") private val fullAgeDays: Long,
    @param:ConfigProperty(name = "cleanup.score.size-ref-gb") private val sizeRefGb: Long,
    @param:ConfigProperty(name = "cleanup.score.weight.last-watched") private val weightLastWatched: Double,
    @param:ConfigProperty(name = "cleanup.score.weight.download-age") private val weightDownloadAge: Double,
    @param:ConfigProperty(name = "cleanup.score.weight.size") private val weightSize: Double,
    @param:ConfigProperty(name = "cleanup.score.weight.completion") private val weightCompletion: Double,
    @param:ConfigProperty(name = "cleanup.score.weight.requester-activity") private val weightRequesterActivity: Double,
) : CleanupConfigStore {
    companion object {
        private const val GB = 1_000_000_000L
    }

    override fun effective(): CleanupConfig = CleanupConfig(
        diskPath = diskPath,
        thresholdFreeBytes = thresholdFreeGb * GB,
        targetFreeBytes = targetFreeGb * GB,
        graceDays = graceDays,
        minAgeDays = minAgeDays,
        recentSeriesWatchDays = recentSeriesWatchDays,
        inProgressDays = inProgressDays,
        maxCandidates = maxCandidates,
        minScore = minScore,
        scoring = ScoringConfig(
            fullAgeDays = fullAgeDays,
            sizeRefBytes = sizeRefGb * GB,
            weightLastWatched = weightLastWatched,
            weightDownloadAge = weightDownloadAge,
            weightSize = weightSize,
            weightCompletion = weightCompletion,
            weightRequesterActivity = weightRequesterActivity,
        ),
    )
}
