package org.hoohoot.homelab.manager.members.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "member")
class MemberEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "authentik_pk", unique = true)
    var authentikPk: Int? = null

    @Column(name = "username", nullable = false)
    lateinit var username: String

    @Column(name = "display_name", nullable = false)
    lateinit var displayName: String

    @Column(name = "email")
    var email: String? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    companion object : PanacheCompanionBase<MemberEntity, UUID>
}
