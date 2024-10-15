package org.hoohoot;

public record MatrixMessage(
        String msgType,
        String body,
        String format,
        String formattedBody
) {
}