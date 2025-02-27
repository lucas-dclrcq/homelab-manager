package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import com.fasterxml.jackson.annotation.JsonProperty
import org.hoohoot.homelab.manager.notifications.domain.NotificationId

data class MatrixMessage(
    @param:JsonProperty("msgtype") val msgType: String,
    val body: String,
    val format: String,
    @param:JsonProperty("formatted_body") val formattedBody: String,
    @param:JsonProperty("m.relates_to") val relatesTo: RelatesTo? = null
) {
    companion object {
        @JvmStatic
        fun html(textContent: String, htmlContent: String, relatesTo: NotificationId? = null): MatrixMessage {
            return MatrixMessage(
                "m.text",
                textContent,
                "org.matrix.custom.html",
                htmlContent,
                if (relatesTo != null) RelatesTo(relatesTo.value) else null
            )
        }
    }
}

data class RelatesTo(@param:JsonProperty("event_id") val eventId: String, @param:JsonProperty("rel_type") val relType: String = "m.thread")
