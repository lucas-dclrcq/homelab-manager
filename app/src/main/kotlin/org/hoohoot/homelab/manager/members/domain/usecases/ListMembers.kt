package org.hoohoot.homelab.manager.members.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.members.domain.ports.Members
import org.hoohoot.homelab.manager.members.infra.MemberEntity

@ApplicationScoped
class ListMembers(
    private val members: Members,
) {
    suspend operator fun invoke(): List<MemberEntity> = members.listAll()
}
