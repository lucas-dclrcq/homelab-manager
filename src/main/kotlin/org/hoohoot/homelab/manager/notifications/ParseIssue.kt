package org.hoohoot.homelab.manager.notifications

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeerrWebhookPayload(
    @JsonProperty("notification_type") val notificationType: String? = null,
    val event: String? = null,
    val subject: String? = null,
    val message: String? = null,
    val issue: SeerrWebhookIssue? = null,
    val comment: SeerrWebhookComment? = null,
    val extra: List<SeerrWebhookExtra>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeerrWebhookIssue(
    @JsonProperty("issue_id") val issueId: String? = null,
    @JsonProperty("reportedBy_username") val reportedByUsername: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeerrWebhookComment(
    @JsonProperty("comment_message") val commentMessage: String? = null,
    @JsonProperty("commentedBy_username") val commentedByUsername: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeerrWebhookExtra(
    val name: String? = null,
    val value: String? = null,
)

private const val DEFAULT_VALUE = "unknown"

data class Issue(
    val notificationType: String,
    val message: String,
    val subject: String,
    val reportedByUserName: String,
    val title: String,
    val id: String,
    val additionalInfo: Map<String, String>,
    val comment: String?,
    val commentedBy: String?
) {
    companion object {
        fun from(payload: SeerrWebhookPayload): Issue = Issue(
            notificationType = payload.notificationType ?: DEFAULT_VALUE,
            message = payload.message ?: DEFAULT_VALUE,
            subject = payload.subject ?: DEFAULT_VALUE,
            reportedByUserName = payload.issue?.reportedByUsername ?: DEFAULT_VALUE,
            title = payload.event ?: DEFAULT_VALUE,
            id = payload.issue?.issueId ?: DEFAULT_VALUE,
            additionalInfo = payload.extra
                ?.filter { it.name != null && it.value != null }
                ?.associate { it.name!! to it.value!! }
                ?: emptyMap(),
            comment = payload.comment?.commentMessage,
            commentedBy = payload.comment?.commentedByUsername
        )
    }
}
