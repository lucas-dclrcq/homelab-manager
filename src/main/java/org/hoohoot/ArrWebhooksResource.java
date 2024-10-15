package org.hoohoot;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/arr/webhooks/radarr")
public class ArrWebhooksResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String radarrWebhook() {
        return "Hello from Quarkus REST";
    }
}
