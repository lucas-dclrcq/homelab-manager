package org.hoohoot.homelab.manager.members.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.members.domain.ports.MemberDirectory
import org.hoohoot.homelab.manager.members.domain.ports.MemberSyncStats
import org.hoohoot.homelab.manager.members.domain.ports.Members

@ApplicationScoped
class SyncMembers(
    private val memberDirectory: MemberDirectory,
    private val members: Members,
) {
    suspend operator fun invoke(): MemberSyncStats {
        val users = memberDirectory.fetchUsers()
        check(users.isNotEmpty()) { "Authentik n'a renvoyé aucun utilisateur : synchronisation abandonnée" }
        val stats = members.syncFromDirectory(users)
        Log.info("Member sync: ${stats.created} créés, ${stats.updated} mis à jour, ${stats.deactivated} désactivés")
        return stats
    }
}
