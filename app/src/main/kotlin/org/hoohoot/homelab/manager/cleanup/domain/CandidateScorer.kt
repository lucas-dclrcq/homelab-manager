package org.hoohoot.homelab.manager.cleanup.domain

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * Scoring pur d'un candidat au nettoyage : plus le score est haut, plus le média est supprimable.
 * Le breakdown complet est retourné pour rester explicable dans l'UI et dans l'annonce Matrix.
 */
class CandidateScorer(
    private val config: CleanupConfig,
    private val now: LocalDateTime,
) {
    companion object {
        // Un média non corrélé à Jellyfin n'est peut-être juste pas reconnu : prudence
        const val UNCORRELATED_CAP = 0.7
        const val REQUESTER_ACTIVITY_REF_DAYS = 180L
    }

    fun evaluate(input: ScoringInput): Evaluation {
        val downloadedAt = input.downloadedAt
            ?: return Evaluation.Excluded("âge du téléchargement inconnu")

        val downloadAgeDays = daysSince(downloadedAt)
        if (downloadAgeDays < config.minAgeDays) {
            return Evaluation.Excluded("téléchargé il y a moins de ${config.minAgeDays} jours")
        }

        val lastInProgressAt = input.watch.lastInProgressAt
        if (lastInProgressAt != null && daysSince(lastInProgressAt) < config.inProgressDays) {
            return Evaluation.Excluded("visionnage en cours")
        }

        return Evaluation.Scored(score(input, downloadAgeDays))
    }

    private fun score(input: ScoringInput, downloadAgeDays: Long): ScoreBreakdown {
        val scoring = config.scoring
        val components = listOf(
            lastWatchedComponent(input.watch, scoring),
            downloadAgeComponent(downloadAgeDays, scoring),
            sizeComponent(input.sizeBytes, scoring),
            completionComponent(input.watch, scoring),
            requesterActivityComponent(input.requester, scoring),
        )

        val totalWeight = components.sumOf { it.weight }
        val total =
            if (totalWeight == 0.0) 0.0
            else (components.sumOf { it.points } / totalWeight * 100).round2()

        return ScoreBreakdown(
            total = total,
            components = components,
            inputs = ScoreInputs(
                lastWatchedAt = input.watch.lastWatchedAt,
                downloadedAt = input.downloadedAt,
                sizeBytes = input.sizeBytes,
                requester = input.requester?.username,
                correlation = input.watch.correlation.name,
            ),
        )
    }

    private fun lastWatchedComponent(watch: CorrelatedWatch, scoring: ScoringConfig): ScoreComponent {
        val (normalized, rawValue) = when {
            watch.lastWatchedAt != null -> {
                val days = daysSince(watch.lastWatchedAt)
                clamp(days.toDouble() / scoring.fullAgeDays) to "il y a $days j"
            }
            watch.correlation == Correlation.NONE -> UNCORRELATED_CAP to "non corrélé à Jellyfin"
            else -> 1.0 to "jamais visionné"
        }
        return component("lastWatched", "Dernier visionnage", scoring.weightLastWatched, rawValue, normalized)
    }

    private fun downloadAgeComponent(downloadAgeDays: Long, scoring: ScoringConfig): ScoreComponent =
        component(
            "downloadAge",
            "Ancienneté du téléchargement",
            scoring.weightDownloadAge,
            "il y a $downloadAgeDays j",
            clamp(downloadAgeDays.toDouble() / scoring.fullAgeDays),
        )

    private fun sizeComponent(sizeBytes: Long, scoring: ScoringConfig): ScoreComponent =
        component(
            "size",
            "Taille",
            scoring.weightSize,
            "${(sizeBytes / 1e8).roundToInt() / 10.0} Go",
            clamp(sizeBytes.toDouble() / scoring.sizeRefBytes),
        )

    private fun completionComponent(watch: CorrelatedWatch, scoring: ScoringConfig): ScoreComponent {
        val (normalized, rawValue) = when {
            watch.completedBySomeone -> 1.0 to "vu en entier"
            watch.startedBySomeone -> 0.6 to "entamé sans être fini"
            else -> 0.5 to "jamais entamé"
        }
        return component("completion", "Complétion", scoring.weightCompletion, rawValue, normalized)
    }

    private fun requesterActivityComponent(requester: RequesterProfile?, scoring: ScoringConfig): ScoreComponent {
        val (normalized, rawValue) = when {
            requester == null -> 0.5 to "demandeur inconnu"
            requester.activeMember == false -> 1.0 to "${requester.username} : membre inactif"
            requester.lastActivityAt != null -> {
                val days = daysSince(requester.lastActivityAt)
                clamp(days.toDouble() / REQUESTER_ACTIVITY_REF_DAYS) to
                    "${requester.username} : actif il y a $days j"
            }
            else -> 1.0 to "${requester.username} : aucune activité"
        }
        return component("requesterActivity", "Activité du demandeur", scoring.weightRequesterActivity, rawValue, normalized)
    }

    private fun component(key: String, label: String, weight: Double, rawValue: String, normalized: Double) =
        ScoreComponent(
            key = key,
            label = label,
            weight = weight,
            rawValue = rawValue,
            normalized = normalized.round2(),
            points = (weight * normalized).round2(),
        )

    private fun daysSince(moment: LocalDateTime): Long = Duration.between(moment, now).toDays().coerceAtLeast(0)

    private fun clamp(value: Double): Double = value.coerceIn(0.0, 1.0)

    private fun Double.round2(): Double = Math.round(this * 100) / 100.0
}
