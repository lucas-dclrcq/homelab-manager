package org.hoohoot.homelab.manager.domain

import io.vertx.core.json.JsonArray

fun JsonArray?.requester(): String? =
    this?.map { it.toString() }
        ?.firstOrNull { it.matches("\\d+ - \\w+".toRegex()) }
        ?.split(" - ")
        ?.get(1)

fun String.toImdbLink(): String = "https://www.imdb.com/title/$this/"