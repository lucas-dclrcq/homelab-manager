package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import com.fasterxml.jackson.annotation.JsonProperty

data class MatrixMessageResponse(@field:JsonProperty("event_id") @param:JsonProperty("event_id") val eventId: String)