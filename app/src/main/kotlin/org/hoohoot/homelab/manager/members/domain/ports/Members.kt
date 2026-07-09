package org.hoohoot.homelab.manager.members.domain.ports

import org.hoohoot.homelab.manager.members.infra.MemberEntity
import java.util.UUID

data class DirectoryUser(
    val pk: Int,
    val username: String,
    val displayName: String,
    val email: String?,
    val active: Boolean,
)

data class MemberSyncStats(val created: Int, val updated: Int, val deactivated: Int)

interface Members {
    suspend fun listAll(): List<MemberEntity>
    suspend fun findById(id: UUID): MemberEntity?
    suspend fun syncFromDirectory(users: List<DirectoryUser>): MemberSyncStats
}
