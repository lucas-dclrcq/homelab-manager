package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "cleanup_protection")
class CleanupProtectionEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "media_kind", nullable = false)
    lateinit var mediaKind: String

    @Column(name = "radarr_movie_id")
    var radarrMovieId: Int? = null

    @Column(name = "sonarr_series_id")
    var sonarrSeriesId: Int? = null

    @Column(name = "season_number")
    var seasonNumber: Int? = null

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "year")
    var year: Int? = null

    @Column(name = "poster_url")
    var posterUrl: String? = null

    @Column(name = "protected_by", nullable = false)
    lateinit var protectedBy: String

    @Column(name = "source", nullable = false)
    lateinit var source: String

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    companion object : PanacheCompanionBase<CleanupProtectionEntity, UUID> {
        const val KIND_MOVIE = "MOVIE"
        const val KIND_SERIES = "SERIES"
        const val KIND_SEASON = "SEASON"

        const val SOURCE_VETO = "VETO"
        const val SOURCE_PROACTIVE = "PROACTIVE"
    }

    // Une protection SERIES couvre toutes les saisons de la série
    fun covers(radarrMovieId: Int?, sonarrSeriesId: Int?, seasonNumber: Int?): Boolean = when (mediaKind) {
        KIND_MOVIE -> radarrMovieId != null && this.radarrMovieId == radarrMovieId
        KIND_SERIES -> sonarrSeriesId != null && this.sonarrSeriesId == sonarrSeriesId
        KIND_SEASON -> sonarrSeriesId != null && this.sonarrSeriesId == sonarrSeriesId &&
            this.seasonNumber == seasonNumber
        else -> false
    }

    // Supprimer une série entière emporterait ses saisons : la protection d'une seule saison suffit à bloquer
    fun blocksDeletionOf(mediaKind: String, radarrMovieId: Int?, sonarrSeriesId: Int?, seasonNumber: Int?): Boolean =
        covers(radarrMovieId, sonarrSeriesId, seasonNumber) ||
            (mediaKind == KIND_SERIES && sonarrSeriesId != null && this.sonarrSeriesId == sonarrSeriesId)
}
