package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "application")
class ApplicationEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "name", nullable = false)
    lateinit var name: String

    @Column(name = "category", nullable = false)
    lateinit var category: String

    @Column(name = "description", nullable = false)
    lateinit var description: String

    @Column(name = "url", nullable = false)
    lateinit var url: String

    @Column(name = "requires_vpn", nullable = false)
    var requiresVpn: Boolean = false

    @Column(name = "logo")
    var logo: ByteArray? = null

    @Column(name = "logo_content_type")
    var logoContentType: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    companion object : PanacheCompanionBase<ApplicationEntity, UUID>
}
