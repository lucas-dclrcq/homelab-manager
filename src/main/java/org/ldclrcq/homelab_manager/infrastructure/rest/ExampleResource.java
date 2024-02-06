package org.ldclrcq.homelab_manager.infrastructure.rest;

import an.awesome.pipelinr.Pipeline;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.ldclrcq.homelab_manager.application.usecase.CreateApp;

@Path("/hello")
public class ExampleResource {

    @Inject
    Pipeline pipeline;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        new CreateApp("michel").execute(pipeline);
        return "Hello from RESTEasy Reactive";
    }
}
