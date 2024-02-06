package org.ldclrcq.homelab_manager.infrastructure.mediator;


import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import an.awesome.pipelinr.Pipelinr;
import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PipelineRConfiguration {

    @ApplicationScoped
    Pipeline buildPipeline(@All List<Command.Handler<?, ?>> commandHandlers) {
        @SuppressWarnings("rawtypes") List<Command.Handler> handlers = new ArrayList<>(commandHandlers);

        return new Pipelinr()
                .with(handlers::stream)
                .with(() -> Stream.of(new LoggingMiddleware()));
    }
}