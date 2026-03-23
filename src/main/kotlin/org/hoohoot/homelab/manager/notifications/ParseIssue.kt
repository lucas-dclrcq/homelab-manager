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

fun SeerrWebhookPayload.notificationType(): String = notificationType ?: DEFAULT_VALUE
fun SeerrWebhookPayload.message(): String = message ?: DEFAULT_VALUE
fun SeerrWebhookPayload.subject(): String = subject ?: DEFAULT_VALUE
fun SeerrWebhookPayload.reportedByUserName(): String = issue?.reportedByUsername ?: DEFAULT_VALUE
fun SeerrWebhookPayload.title(): String = event ?: DEFAULT_VALUE
fun SeerrWebhookPayload.issueId(): String = issue?.issueId ?: DEFAULT_VALUE
fun SeerrWebhookPayload.additionalInfo(): Map<String, String> = extra
    ?.filter { it.name != null && it.value != null }
    ?.associate { it.name!! to it.value!! }
    ?: emptyMap()
fun SeerrWebhookPayload.commentMessage(): String? = comment?.commentMessage
fun SeerrWebhookPayload.commentedBy(): String? = comment?.commentedByUsername
