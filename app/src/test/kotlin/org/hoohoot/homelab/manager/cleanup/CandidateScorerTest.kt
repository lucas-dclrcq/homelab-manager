package org.hoohoot.homelab.manager.cleanup

import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.cleanup.domain.CandidateScorer
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.domain.CorrelatedWatch
import org.hoohoot.homelab.manager.cleanup.domain.Correlation
import org.hoohoot.homelab.manager.cleanup.domain.Evaluation
import org.hoohoot.homelab.manager.cleanup.domain.RequesterProfile
import org.hoohoot.homelab.manager.cleanup.domain.ScoreBreakdown
import org.hoohoot.homelab.manager.cleanup.domain.ScoringConfig
import org.hoohoot.homelab.manager.cleanup.domain.ScoringInput
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class CandidateScorerTest {

    private val now: LocalDateTime = LocalDateTime.parse("2026-07-01T12:00:00")
    private val scorer = CandidateScorer(config(), now)

    private fun scoring(
        weightLastWatched: Double = 35.0,
        weightDownloadAge: Double = 20.0,
        weightSize: Double = 20.0,
        weightCompletion: Double = 15.0,
        weightRequesterActivity: Double = 10.0,
    ) = ScoringConfig(
        fullAgeDays = 365,
        sizeRefBytes = 30_000_000_000,
        weightLastWatched = weightLastWatched,
        weightDownloadAge = weightDownloadAge,
        weightSize = weightSize,
        weightCompletion = weightCompletion,
        weightRequesterActivity = weightRequesterActivity,
    )

    private fun config(scoring: ScoringConfig = scoring()) = CleanupConfig(
        diskPath = "/data",
        thresholdFreeBytes = 100_000_000_000,
        targetFreeBytes = 200_000_000_000,
        graceDays = 3,
        suggestionGraceDays = 2,
        minAgeDays = 60,
        recentSeriesWatchDays = 90,
        inProgressDays = 30,
        maxCandidates = 20,
        minScore = 40.0,
        scoring = scoring,
    )

    private fun watch(
        correlation: Correlation = Correlation.PROVIDER_ID,
        lastWatchedAt: LocalDateTime? = null,
        completedBySomeone: Boolean = false,
        startedBySomeone: Boolean = false,
        lastInProgressAt: LocalDateTime? = null,
    ) = CorrelatedWatch(
        correlation = correlation,
        lastWatchedAt = lastWatchedAt,
        completedBySomeone = completedBySomeone,
        startedBySomeone = startedBySomeone,
        lastInProgressAt = lastInProgressAt,
    )

    private fun input(
        sizeBytes: Long = 30_000_000_000,
        downloadedAt: LocalDateTime? = now.minusDays(365),
        watch: CorrelatedWatch = watch(),
        requester: RequesterProfile? = null,
    ) = ScoringInput(
        sizeBytes = sizeBytes,
        downloadedAt = downloadedAt,
        watch = watch,
        requester = requester,
    )

    private fun Evaluation.breakdown(): ScoreBreakdown = (this as Evaluation.Scored).breakdown

    private fun ScoreBreakdown.component(key: String) = components.single { it.key == key }

    @Test
    fun `un media jamais visionne, vieux d'un an et volumineux obtient un score eleve`() {
        val breakdown = scorer.evaluate(input()).breakdown()

        val lastWatched = breakdown.component("lastWatched")
        assertThat(lastWatched.normalized).isEqualTo(1.0)
        assertThat(lastWatched.points).isEqualTo(35.0)
        assertThat(breakdown.total).isEqualTo(87.5)
        assertThat(breakdown.total).isEqualTo(breakdown.components.sumOf { it.points })
    }

    @Test
    fun `un media telecharge il y a moins de minAgeDays est exclu`() {
        val result = scorer.evaluate(input(downloadedAt = now.minusDays(10)))

        assertThat(result).isInstanceOf(Evaluation.Excluded::class.java)
        assertThat((result as Evaluation.Excluded).reason).contains("60")
    }

    @Test
    fun `un media sans date de telechargement est exclu`() {
        val result = scorer.evaluate(input(downloadedAt = null))

        assertThat(result).isInstanceOf(Evaluation.Excluded::class.java)
    }

    @Test
    fun `un visionnage en cours recent exclut le media`() {
        val result = scorer.evaluate(
            input(watch = watch(startedBySomeone = true, lastInProgressAt = now.minusDays(5))),
        )

        assertThat(result).isInstanceOf(Evaluation.Excluded::class.java)
        assertThat((result as Evaluation.Excluded).reason).isEqualTo("visionnage en cours")
    }

    @Test
    fun `un media visionne hier a une composante lastWatched proche de zero`() {
        val breakdown = scorer.evaluate(
            input(watch = watch(lastWatchedAt = now.minusDays(1), startedBySomeone = true)),
        ).breakdown()

        val lastWatched = breakdown.component("lastWatched")
        assertThat(lastWatched.normalized).isEqualTo(0.0)
        assertThat(lastWatched.points).isLessThan(1.0)
    }

    @Test
    fun `un media non correle jamais visionne est plafonne par prudence`() {
        val breakdown = scorer.evaluate(
            input(watch = watch(correlation = Correlation.NONE)),
        ).breakdown()

        assertThat(breakdown.component("lastWatched").normalized).isEqualTo(0.7)
    }

    @Test
    fun `un demandeur membre inactif maximise la composante d'activite`() {
        val breakdown = scorer.evaluate(
            input(requester = RequesterProfile("bob", activeMember = false, lastActivityAt = now.minusDays(2))),
        ).breakdown()

        assertThat(breakdown.component("requesterActivity").normalized).isEqualTo(1.0)
    }

    @Test
    fun `un poids a zero neutralise sa composante et le total reste normalise sur les poids restants`() {
        val scorer = CandidateScorer(config(scoring(weightRequesterActivity = 0.0)), now)

        val breakdown = scorer.evaluate(input()).breakdown()

        assertThat(breakdown.component("requesterActivity").points).isEqualTo(0.0)
        // 82.5 points sur 90 de poids -> 91.67 une fois renormalisé
        assertThat(breakdown.total).isEqualTo(91.67)
    }

    @Test
    fun `la completion vaut 1 si vu en entier, 0_6 si entame, 0_5 sinon`() {
        val completed = scorer.evaluate(
            input(watch = watch(completedBySomeone = true, startedBySomeone = true)),
        ).breakdown()
        val started = scorer.evaluate(
            input(watch = watch(startedBySomeone = true)),
        ).breakdown()
        val untouched = scorer.evaluate(input()).breakdown()

        assertThat(completed.component("completion").normalized).isEqualTo(1.0)
        assertThat(started.component("completion").normalized).isEqualTo(0.6)
        assertThat(untouched.component("completion").normalized).isEqualTo(0.5)
    }
}
