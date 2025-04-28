package org.hoohoot.homelab.manager.infrastructure.api.resources;

import com.trendyol.kediatr.Mediator
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.queries.GetUserPlaylists
import org.hoohoot.homelab.manager.application.queries.PlaylistDto

@Path("/api/spotify/playlists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Import Playlist", description = "Import playlist from spotify")
class PlaylistResource(private val mediator: Mediator) {


    @GET
    @Operation(summary = "Get playlists from your a specific spotify user")
    suspend fun getUserPlaylists(): List<PlaylistDto>? {
        return this.mediator.send(GetUserPlaylists())
    }

//    @POST
//    @Operation(summary = "Download tracks and albums")
//    suspend fun download(plaulists : List<PlaylistDto>) {
//        return this.mediator.send(DownloadPlaylists())
//    }

}
