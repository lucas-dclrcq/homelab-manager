package org.hoohoot.homelab.manager;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hoohoot.homelab.manager.matrix.MatrixAPI;
import org.hoohoot.homelab.manager.matrix.MatrixMessage;
import org.hoohoot.homelab.manager.radarr.RadarrMovieDownloadedDTO;

import java.util.UUID;

@Path("/api/arr/webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArrWebhooksResource {
    private final MatrixAPI matrixAPI;
    private final String matrixRoomId;

    public ArrWebhooksResource(@RestClient MatrixAPI matrixAPI, @ConfigProperty(name = "matrix.room_id") String matrixRoomId) {
        this.matrixAPI = matrixAPI;
        this.matrixRoomId = matrixRoomId;
    }

    @Path("test")
    @GET
    public Response test() {
        return Response.ok().entity("Ok !").build();
    }
    @POST
    @Path("/radarr")
    public Uni<Response> radarrWebhook(RadarrMovieDownloadedDTO radarrMovieDownloadedNotification) {
        return switch (radarrMovieDownloadedNotification.eventType()) {
            case "Download" -> buildDownloadedMovieMatrixMessage(radarrMovieDownloadedNotification);
            case "Test" -> this.matrixAPI.sendMessage(this.matrixRoomId, UUID.randomUUID().toString(), MatrixMessage.html("Howdy this is a test notification from Radarr!"));
            default ->
                    throw new IllegalStateException("Unexpected value: " + radarrMovieDownloadedNotification.eventType());
        };
    }

    private Uni<Response> buildDownloadedMovieMatrixMessage(RadarrMovieDownloadedDTO radarrMovieDownloadedNotification) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h1>Movie Downloaded</h1>");
        stringBuilder.append("<p>");
        stringBuilder.append("%s (%s) [WEBDL-1080p] https://www.imdb.com/title/%s/<br>".formatted(radarrMovieDownloadedNotification.movie().title(), radarrMovieDownloadedNotification.movie().year(), radarrMovieDownloadedNotification.movie().imdbId()));
        stringBuilder.append("Requested by : %s".formatted(radarrMovieDownloadedNotification.movie().userTag()));
        stringBuilder.append("</p>");

        return this.matrixAPI.sendMessage(this.matrixRoomId, UUID.randomUUID().toString(), MatrixMessage.html(stringBuilder.toString()));
    }
}
