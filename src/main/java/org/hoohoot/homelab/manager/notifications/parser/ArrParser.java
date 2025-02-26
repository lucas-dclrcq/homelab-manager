package org.hoohoot.homelab.manager.notifications.parser;

import io.vertx.core.json.JsonArray;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class ArrParser {
    protected final Optional<JsonArray> tags;

    protected ArrParser(Optional<JsonArray> tags) {
        this.tags = tags;
    }

    public String requester() {
        return tags.flatMap(tags -> tags.stream().map(Object::toString)
                        .filter(tag -> tag.matches("\\d+ - \\w+"))
                        .map(tag -> tag.split(" - ")[1])
                        .findFirst())
                .orElse("unknown");
    }}
