package org.hoohoot.homelab.manager.cleanup.domain

import java.util.UUID

data class MatchableCandidate(
    val candidateId: UUID,
    val title: String,
    val mediaKind: String,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
)

sealed interface TitleMatch {
    data class Matched(val candidates: List<MatchableCandidate>) : TitleMatch
    data class Ambiguous(val titles: List<String>) : TitleMatch
    data object NoMatch : TitleMatch
}

class TitleMatcher {
    fun match(query: String, candidates: List<MatchableCandidate>): TitleMatch {
        val normalizedQuery = Titles.normalize(query)
        if (normalizedQuery.isBlank() || candidates.isEmpty()) return TitleMatch.NoMatch

        val byNormalizedTitle = candidates.map { it to Titles.normalize(it.title) }

        val bestTier = listOf<(String) -> Boolean>(
            { it == normalizedQuery },
            { it.startsWith(normalizedQuery) },
            { it.contains(normalizedQuery) },
        )
            .firstNotNullOfOrNull { predicate ->
                byNormalizedTitle.filter { (_, title) -> predicate(title) }
                    .map { (candidate, _) -> candidate }
                    .ifEmpty { null }
            }
            ?: return TitleMatch.NoMatch

        val byMedia = bestTier.groupBy { it.mediaKey() }
        return if (byMedia.size == 1) {
            TitleMatch.Matched(byMedia.values.single())
        } else {
            TitleMatch.Ambiguous(bestTier.map { it.title }.distinct())
        }
    }

    private fun MatchableCandidate.mediaKey(): String = when {
        radarrMovieId != null -> "movie:$radarrMovieId"
        sonarrSeriesId != null -> "series:$sonarrSeriesId"
        else -> "candidate:$candidateId"
    }
}
