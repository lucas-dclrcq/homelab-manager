package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import com.fasterxml.jackson.annotation.JsonProperty

data class MatrixMessageResponse(@param:JsonProperty("event_id") val eventId: String)