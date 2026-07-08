package org.hoohoot.homelab.manager.notifications.api

private val REQUESTER_REGEX = "\\d+ - \\w+".toRegex()

fun List<String>?.requester(): String? =
    this?.firstOrNull { it.matches(REQUESTER_REGEX) }
        ?.split(" - ")
        ?.get(1)

fun String.toImdbLink(): String = "https://www.imdb.com/title/$this/"
