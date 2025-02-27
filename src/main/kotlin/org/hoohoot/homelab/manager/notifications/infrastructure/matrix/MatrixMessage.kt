package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

data class MatrixMessage(
    val msgType: String,
    val body: String,
    val format: String,
    val formattedBody: String,
    val relatesTo: RelatesTo? = null
) {
    companion object {
        @JvmStatic
        fun html(textContent: String, htmlContent: String): MatrixMessage {
            return MatrixMessage("m.text", textContent, "org.matrix.custom.html", htmlContent)
        }
    }
}

data class RelatesTo(val eventId: String, val relType: String = "m.thread")
