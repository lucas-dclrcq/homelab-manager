package org.hoohoot.homelab.manager.cleanup

import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.cleanup.domain.MatchableCandidate
import org.hoohoot.homelab.manager.cleanup.domain.TitleMatch
import org.hoohoot.homelab.manager.cleanup.domain.TitleMatcher
import org.junit.jupiter.api.Test
import java.util.UUID

internal class TitleMatcherTest {

    private val matcher = TitleMatcher()

    private fun candidate(
        title: String,
        radarrMovieId: Int? = null,
        sonarrSeriesId: Int? = null,
        seasonNumber: Int? = null,
    ) = MatchableCandidate(
        candidateId = UUID.randomUUID(),
        title = title,
        mediaKind = if (radarrMovieId != null) "MOVIE" else "SEASON",
        radarrMovieId = radarrMovieId,
        sonarrSeriesId = sonarrSeriesId,
        seasonNumber = seasonNumber,
    )

    @Test
    fun `le match exact l'emporte sur les prefixes et le contenu`() {
        val dune = candidate("Dune", radarrMovieId = 1)
        val duneTwo = candidate("Dune Part Two", radarrMovieId = 2)

        val match = matcher.match("dune", listOf(dune, duneTwo))

        assertThat(match).isEqualTo(TitleMatch.Matched(listOf(dune)))
    }

    @Test
    fun `sans match exact le prefixe l'emporte sur le contenu`() {
        val duneTwo = candidate("Dune Part Two", radarrMovieId = 2)
        val grandeDune = candidate("La Grande Dune", radarrMovieId = 3)

        val match = matcher.match("dune", listOf(duneTwo, grandeDune))

        assertThat(match).isEqualTo(TitleMatch.Matched(listOf(duneTwo)))
    }

    @Test
    fun `le contenu sert de dernier recours`() {
        val grandeDune = candidate("La Grande Dune", radarrMovieId = 3)

        val match = matcher.match("dune", listOf(grandeDune))

        assertThat(match).isEqualTo(TitleMatch.Matched(listOf(grandeDune)))
    }

    @Test
    fun `accents et ponctuation sont ignores`() {
        val amelie = candidate("Amélie !", radarrMovieId = 4)

        val match = matcher.match("AMELIE", listOf(amelie))

        assertThat(match).isEqualTo(TitleMatch.Matched(listOf(amelie)))
    }

    @Test
    fun `plusieurs saisons candidates d'une meme serie forment un seul match`() {
        val seasonOne = candidate("Breaking Bad", sonarrSeriesId = 10, seasonNumber = 1)
        val seasonTwo = candidate("Breaking Bad", sonarrSeriesId = 10, seasonNumber = 2)

        val match = matcher.match("breaking bad", listOf(seasonOne, seasonTwo))

        assertThat(match).isInstanceOf(TitleMatch.Matched::class.java)
        assertThat((match as TitleMatch.Matched).candidates).containsExactlyInAnyOrder(seasonOne, seasonTwo)
    }

    @Test
    fun `deux medias distincts au meme palier donnent un match ambigu avec les titres distincts`() {
        val episodeFour = candidate("Star Wars Episode IV", radarrMovieId = 1)
        val episodeFive = candidate("Star Wars Episode V", radarrMovieId = 2)

        val match = matcher.match("star wars", listOf(episodeFour, episodeFive))

        assertThat(match).isEqualTo(TitleMatch.Ambiguous(listOf("Star Wars Episode IV", "Star Wars Episode V")))
    }

    @Test
    fun `aucune correspondance ou requete blanche donne NoMatch`() {
        val dune = candidate("Dune", radarrMovieId = 1)

        assertThat(matcher.match("interstellar", listOf(dune))).isEqualTo(TitleMatch.NoMatch)
        assertThat(matcher.match("   ", listOf(dune))).isEqualTo(TitleMatch.NoMatch)
        assertThat(matcher.match("dune", emptyList())).isEqualTo(TitleMatch.NoMatch)
    }
}
