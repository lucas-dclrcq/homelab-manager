package org.hoohoot.homelab.manager.it.config

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.vertx.VertxContextSupport
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/** Seed des tables cleanup_* pour les tests IT (campagnes, candidats, protections). */
internal object CleanupSeed {

    const val GB = 1_000_000_000L

    fun insertCampaign(
        status: String = CleanupCampaignEntity.STATUS_ANNOUNCED,
        graceEndsAt: LocalDateTime = LocalDateTime.now().plusDays(14),
        freeBytesAtStart: Long = 50 * GB,
        targetBytesToFree: Long = 250 * GB,
    ): UUID {
        val campaignId = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CleanupCampaignEntity().apply {
                    id = campaignId
                    this.status = status
                    triggerType = "AUTO"
                    diskPath = "/data"
                    this.freeBytesAtStart = freeBytesAtStart
                    thresholdBytes = 200 * GB
                    this.targetBytesToFree = targetBytesToFree
                    this.graceEndsAt = graceEndsAt
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }.persist<CleanupCampaignEntity>()
            }
        }
        return campaignId
    }

    fun insertCandidate(
        campaignId: UUID,
        title: String,
        mediaKind: String = CleanupCandidateEntity.KIND_MOVIE,
        radarrMovieId: Int? = null,
        sonarrSeriesId: Int? = null,
        seasonNumber: Int? = null,
        sizeBytes: Long = 40 * GB,
        score: Double = 80.0,
        status: String = CleanupCandidateEntity.STATUS_PROPOSED,
    ): UUID {
        val candidateId = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CleanupCandidateEntity().apply {
                    id = candidateId
                    this.campaignId = campaignId
                    this.mediaKind = mediaKind
                    this.radarrMovieId = radarrMovieId
                    this.sonarrSeriesId = sonarrSeriesId
                    this.seasonNumber = seasonNumber
                    this.title = title
                    this.sizeBytes = sizeBytes
                    this.score = BigDecimal.valueOf(score)
                    this.status = status
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }.persist<CleanupCandidateEntity>()
            }
        }
        return candidateId
    }

    fun insertProtection(
        mediaKind: String,
        title: String,
        radarrMovieId: Int? = null,
        sonarrSeriesId: Int? = null,
        seasonNumber: Int? = null,
        protectedBy: String = "alice",
    ): UUID {
        val protectionId = UUID.randomUUID()
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CleanupProtectionEntity().apply {
                    id = protectionId
                    this.mediaKind = mediaKind
                    this.radarrMovieId = radarrMovieId
                    this.sonarrSeriesId = sonarrSeriesId
                    this.seasonNumber = seasonNumber
                    this.title = title
                    this.protectedBy = protectedBy
                    source = CleanupProtectionEntity.SOURCE_VETO
                    createdAt = LocalDateTime.now()
                }.persist<CleanupProtectionEntity>()
            }
        }
        return protectionId
    }

    fun candidateStatus(candidateId: UUID): String? =
        VertxContextSupport.subscribeAndAwait {
            Panache.withSession { CleanupCandidateEntity.findById(candidateId) }
        }?.status

    fun deleteAll() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                CleanupCandidateEntity.deleteAll()
                    .chain { _ -> CleanupCampaignEntity.deleteAll() }
                    .chain { _ -> CleanupProtectionEntity.deleteAll() }
            }
        }
    }
}
