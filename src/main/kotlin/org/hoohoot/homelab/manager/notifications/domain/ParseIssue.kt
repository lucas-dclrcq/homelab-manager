package org.hoohoot.homelab.manager.notifications.domain

import io.vertx.core.json.JsonObject

private const val DEFAULT_VALUE = "unknown"

data class Issue(
    val notificationType: String,
    val message: String,
    val subject: String,
    val reportedByUserName: String,
    val title: String,
    val id: String,
    val additionalInfo: Map<String, String>
)

class ParseIssue private constructor(private val payload: JsonObject) {

    companion object {
        @JvmStatic
        fun from(payload: JsonObject): Issue {
            val parseIssue = ParseIssue(payload)
            return Issue(
                parseIssue.notificationType(),
                parseIssue.message(),
                parseIssue.subject(),
                parseIssue.reportedByUserName(),
                parseIssue.title(),
                parseIssue.issueId(),
                parseIssue.additionalInfo()
            )
        }
    }

    private fun notificationType() = payload.getString("notification_type") ?: DEFAULT_VALUE

    private fun message() = payload.getString("message") ?: DEFAULT_VALUE

    private fun subject() = payload.getString("subject") ?: DEFAULT_VALUE

    private fun reportedByUserName() = payload.getJsonObject("issue")?.getString("reportedBy_username") ?: DEFAULT_VALUE

    private fun title() = payload.getString("event") ?: DEFAULT_VALUE

    private fun issueId() = payload.getJsonObject("issue")?.getString("issue_id")!!

    private fun additionalInfo() = payload.getJsonArray("extra")
        ?.map { it as JsonObject }
        ?.associate { it.getString("name") to it.getString("value") }
        ?: emptyMap()
}