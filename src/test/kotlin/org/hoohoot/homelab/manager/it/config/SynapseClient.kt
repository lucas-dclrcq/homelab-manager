package org.hoohoot.homelab.manager.it.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SynapseClient(
    private val synapseUrl: String,
    private val accessToken: String,
    private val roomIds: Map<String, String>,
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {
    fun getMessages(roomId: String): List<JsonNode> {
        // Synapse accepts /messages without 'from' param when dir=b (starts from most recent).
        // This is not strictly spec-compliant but works with pinned Synapse v1.120.2.
        val response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_matrix/client/r0/rooms/$roomId/messages?dir=b&limit=50"))
                .header("Authorization", "Bearer $accessToken")
                .GET().build(),
            HttpResponse.BodyHandlers.ofString()
        )
        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to get messages from room $roomId: ${response.body()}")
        }
        return objectMapper.readTree(response.body())
            .get("chunk")
            .filter { it.get("type").asText() == "m.room.message" }
    }

    fun getLastMessage(roomId: String): JsonNode {
        val messages = getMessages(roomId)
        if (messages.isEmpty()) throw RuntimeException("No messages found in room $roomId")
        return messages.first().get("content")
    }

    fun getLastMessageEvent(roomId: String): JsonNode {
        val messages = getMessages(roomId)
        if (messages.isEmpty()) throw RuntimeException("No messages found in room $roomId")
        return messages.first()
    }

    fun getMessageCount(roomId: String): Int = getMessages(roomId).size

    fun roomId(name: String): String = roomIds[name]
        ?: throw RuntimeException("Room '$name' not found. Available: ${roomIds.keys}")
}
