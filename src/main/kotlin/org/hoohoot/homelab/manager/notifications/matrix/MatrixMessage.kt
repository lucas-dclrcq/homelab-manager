package org.hoohoot.homelab.manager.notifications.matrix

import com.fasterxml.jackson.annotation.JsonProperty

data class MatrixMessage(
    @field:JsonProperty("msgtype") @param:JsonProperty("msgtype") val msgType: String,
    val body: String,
    val format: String,
    @field:JsonProperty("formatted_body") @param:JsonProperty("formatted_body") val formattedBody: String
) {
    companion object {
        @JvmStatic
        fun html(body: String): MatrixMessage {
            return MatrixMessage("m.text", body, "org.matrix.custom.html", body)
        }
    }
}