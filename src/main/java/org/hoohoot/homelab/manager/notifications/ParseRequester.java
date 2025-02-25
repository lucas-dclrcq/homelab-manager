package org.hoohoot.homelab.manager.notifications;

import io.vertx.core.json.JsonArray;

public class ParseRequester {
    public static String fromTags(JsonArray tags) {
        if (tags == null) return "unknown";

        return tags.stream()
                .map(Object::toString)
                .filter(tag -> tag.matches("\\d+ - \\w+"))
                .map(tag -> tag.split(" - ")[1])
                .findFirst()
                .orElse("unknown");
    }}
