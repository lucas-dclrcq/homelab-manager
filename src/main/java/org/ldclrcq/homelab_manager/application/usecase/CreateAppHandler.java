package org.ldclrcq.homelab_manager.application.usecase;

import an.awesome.pipelinr.Command;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateAppHandler implements Command.Handler<CreateApp, String> {

    @Override
    public String handle(CreateApp createApp) {
        System.out.println("Handling command");
        return createApp.name();
    }
}
