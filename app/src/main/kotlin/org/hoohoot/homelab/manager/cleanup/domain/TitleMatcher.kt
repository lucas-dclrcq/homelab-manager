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
    // Tous les candidats d'un même média : un film, ou les saisons candidates d'une même série
    data class Matched(val candidates: List<MatchableCandidate>) : TitleMatch
    data class Ambiguous(val titles: List<String>) : TitleMatch
    data object NoMatch : TitleMatch
}

/**
 * Matching du titre donné au bot (`!johnny garde <titre>`) contre les candidats de la campagne
 * active : égalité exacte > préfixe > contient, sur titres normalisés. Plusieurs médias distincts
 * dans le meilleur palier -> ambigu, l'utilisateur doit préciser.
 */
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
