package org.ldclrcq.homelab_manager.infrastructure.mediator;

import an.awesome.pipelinr.Command;

class LoggingMiddleware implements Command.Middleware {

    @Override
    public <R, C extends Command<R>> R invoke(C command, Next<R> next) {
        System.out.println("Executing command : " + command.toString());
        R response = next.invoke();
        System.out.println("Command response : " + response.toString());
        return response;
    }
}