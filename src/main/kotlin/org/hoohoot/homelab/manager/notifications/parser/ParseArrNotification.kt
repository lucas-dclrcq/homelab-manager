package org.hoohoot.homelab.manager.notifications.parser

import io.vertx.core.json.JsonArray

fun JsonArray?.requester(): String? =
    this?.map { it.toString() }
        ?.first { it.matches("\\d+ - \\w+".toRegex()) }
        ?.split(" - ")
        ?.get(1)