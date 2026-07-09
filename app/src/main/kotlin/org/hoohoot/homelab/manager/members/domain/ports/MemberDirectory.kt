package org.hoohoot.homelab.manager.members.domain.ports

interface MemberDirectory {
    suspend fun fetchUsers(): List<DirectoryUser>
}
