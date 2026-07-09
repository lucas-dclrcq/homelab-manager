package org.hoohoot.homelab.manager.members.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.members.domain.usecases.ListMembers
import org.hoohoot.homelab.manager.members.infra.MemberEntity
import java.util.UUID

data class MemberDto(
    val id: UUID,
    val username: String,
    val displayName: String,
    val email: String?,
    val active: Boolean,
    val fromAuthentik: Boolean,
)

fun MemberEntity.toDto() = MemberDto(
    id = id!!,
    username = username,
    displayName = displayName,
    email = email,
    active = active,
    fromAuthentik = authentikPk != null,
)

@Path("/api/members")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Membres")
class MembersResource(
    private val listMembersUseCase: ListMembers,
) {

    @GET
    suspend fun listMembers(): List<MemberDto> = listMembersUseCase().map { it.toDto() }
}
