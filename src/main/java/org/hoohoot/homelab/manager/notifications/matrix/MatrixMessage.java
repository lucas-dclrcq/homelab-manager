package org.hoohoot.homelab.manager.notifications.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatrixMessage(
        @JsonProperty("msgtype") String msgType,
        String body,
        String format,
        @JsonProperty("formatted_body") String formattedBody
) {
    public static MatrixMessage html(String body) {
        return new MatrixMessage("m.text", body, "org.matrix.custom.html", body);
    }
}