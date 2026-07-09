package org.hoohoot.homelab.manager.members.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.members.domain.ports.DirectoryUser
import org.hoohoot.homelab.manager.members.domain.ports.MemberDirectory
import org.hoohoot.homelab.manager.shared.authentik.AuthentikRestClient
import java.util.Optional

private const val PAGE_SIZE = 100

@ApplicationScoped
class AuthentikMemberDirectory(
    @param:RestClient private val authentikRestClient: AuthentikRestClient,
    @param:ConfigProperty(name = "member-sync.group") private val group: Optional<String>,
) : MemberDirectory {

    override suspend fun fetchUsers(): List<DirectoryUser> {
        val groupFilter = group.orElse("").takeIf { it.isNotBlank() }
        val users = mutableListOf<DirectoryUser>()
        var page = 1
        while (true) {
            val response = authentikRestClient.getUsers(page, PAGE_SIZE, groupFilter)
            users += response.results.map { user ->
                DirectoryUser(
                    pk = user.pk,
                    username = user.username,
                    displayName = user.name?.takeIf { it.isNotBlank() } ?: user.username,
                    email = user.email?.takeIf { it.isNotBlank() },
                    active = user.isActive,
                )
            }
            // Authentik renvoie next=0 sur la dernière page
            val next = response.pagination.next ?: 0
            if (next <= page) break
            page = next
        }
        return users
    }
}
