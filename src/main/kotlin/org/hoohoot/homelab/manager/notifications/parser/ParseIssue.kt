package org.hoohoot.homelab.manager.notifications.parser

import io.vertx.core.json.JsonObject

private const val DEFAULT_VALUE = "unknown"

class ParseIssue private constructor(private val payload : JsonObject) {

    companion object {
        @JvmStatic
        fun from(payload: JsonObject) = ParseIssue(payload)
    }

    fun message(): String {
        return payload.getString("message") ?: DEFAULT_VALUE
    }

    fun subject(): String {
        return payload.getString("subject") ?: DEFAULT_VALUE
    }

    fun reportedByUserName(): String {
        return payload.getJsonObject("issue")?.getString("reportedBy_username") ?: DEFAULT_VALUE
    }

    fun title(): String {
        return payload.getString("event") ?: DEFAULT_VALUE
    }


}