package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import com.fasterxml.jackson.annotation.JsonProperty

data class MatrixMessageResponse(@param:JsonProperty("event_id") val eventId: String)