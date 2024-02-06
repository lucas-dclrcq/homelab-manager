package org.ldclrcq.homelab_manager.application.usecase;

import an.awesome.pipelinr.Command;

public record CreateApp(String name) implements Command<String> {

}

