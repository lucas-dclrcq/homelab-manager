package org.hoohoot.homelab.manager.cleanup.infra

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

internal fun String?.toUtcLocalDateTime(): LocalDateTime? = this?.let {
    runCatching { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) }.getOrNull()
}
