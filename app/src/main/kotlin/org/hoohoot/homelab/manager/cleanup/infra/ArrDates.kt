package org.hoohoot.homelab.manager.cleanup.infra

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

// Les APIs *arr renvoient des instants ISO ; la base raisonne en UTC (cf. playback_session)
internal fun String?.toUtcLocalDateTime(): LocalDateTime? = this?.let {
    runCatching { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) }.getOrNull()
}
