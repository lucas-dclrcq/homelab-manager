package org.hoohoot.homelab.manager.it.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SynapseClient(
    val synapseUrl: String,
    val accessToken: String,
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {
    fun getMessages(roomId: String): List<JsonNode> {
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

    fun getReactions(roomId: String): List<JsonNode> {
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
            .filter { it.get("type").asText() == "m.reaction" }
    }

    fun getLastReaction(roomId: String): JsonNode {
        val reactions = getReactions(roomId)
        if (reactions.isEmpty()) throw RuntimeException("No reactions found in room $roomId")
        return reactions.first()
    }

    fun getMessageCount(roomId: String): Int = getMessages(roomId).size

    fun sendMessage(roomId: String, body: String): String {
        val msgBody = objectMapper.writeValueAsString(
            mapOf(
                "msgtype" to "m.text",
                "body" to body
            )
        )

        val txnId = System.nanoTime().toString()
        val response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_matrix/client/r0/rooms/$roomId/send/m.room.message/$txnId"))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(msgBody))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to send message to room $roomId: ${response.body()}")
        }

        return objectMapper.readTree(response.body()).get("event_id").asText()
    }

    fun inviteUser(roomId: String, userId: String) {
        val body = objectMapper.writeValueAsString(mapOf("user_id" to userId))

        val response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_matrix/client/r0/rooms/$roomId/invite"))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to invite user $userId to room $roomId: ${response.body()}")
        }
    }

    fun waitForBotMessage(roomId: String, botUserId: String = "@johnnybot:localhost", timeoutMs: Long = 15000): JsonNode {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastMessages: List<JsonNode> = emptyList()
        while (System.currentTimeMillis() < deadline) {
            val messages = getMessages(roomId)
            lastMessages = messages
            val botMessage = messages.firstOrNull {
                it.get("type").asText() == "m.room.message" &&
                    it.get("sender").asText() == botUserId
            }
            if (botMessage != null) {
                return botMessage.get("content")
            }
            Thread.sleep(500)
        }
        val debugInfo = lastMessages.map { "${it.get("sender")?.asText()}: ${it.get("content")?.get("body")?.asText()}" }
        throw RuntimeException("Timed out waiting for bot message in room $roomId. Messages found: $debugInfo")
    }

    fun waitForReaction(roomId: String, timeoutMs: Long = 15000): JsonNode {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val reactions = getReactions(roomId)
            if (reactions.isNotEmpty()) {
                return reactions.first()
            }
            Thread.sleep(500)
        }
        throw RuntimeException("Timed out waiting for reaction in room $roomId")
    }

    fun createRoom(name: String): String {
        val body = objectMapper.writeValueAsString(
            mapOf(
                "name" to name,
                "visibility" to "private"
            )
        )

        val response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_matrix/client/r0/createRoom"))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to create room '$name': ${response.body()}")
        }

        return objectMapper.readTree(response.body()).get("room_id").asText()
    }
}
