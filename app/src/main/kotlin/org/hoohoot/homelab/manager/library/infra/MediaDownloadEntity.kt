package org.hoohoot.homelab.manager.library.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "media_download")
class MediaDownloadEntity : PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "source", nullable = false)
    lateinit var source: String

    @Column(name = "external_id", nullable = false)
    lateinit var externalId: String

    @Column(name = "media_type", nullable = false)
    lateinit var mediaType: String

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "season_number")
    var seasonNumber: Int? = null

    @Column(name = "episode_number")
    var episodeNumber: Int? = null

    @Column(name = "episode_title")
    var episodeTitle: String? = null

    @Column(name = "quality")
    var quality: String? = null

    @Column(name = "language")
    var language: String? = null

    @Column(name = "provider")
    var provider: String? = null

    @Column(name = "artist")
    var artist: String? = null

    @Column(name = "downloaded_at", nullable = false)
    lateinit var downloadedAt: LocalDateTime

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    companion object : PanacheCompanionBase<MediaDownloadEntity, Long> {
        const val SOURCE_RADARR = "radarr"
        const val SOURCE_SONARR = "sonarr"
        const val SOURCE_BAZARR = "bazarr"
        const val SOURCE_LIDARR = "lidarr"

        const val MEDIA_TYPE_MOVIE = "movie"
        const val MEDIA_TYPE_EPISODE = "episode"
        const val MEDIA_TYPE_SUBTITLES = "subtitles"
        const val MEDIA_TYPE_ALBUM = "album"
    }
}
