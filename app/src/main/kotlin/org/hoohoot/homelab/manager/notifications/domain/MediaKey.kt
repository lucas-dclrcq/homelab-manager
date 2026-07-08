package org.hoohoot.homelab.manager.notifications.domain

fun mediaKey(title: String, year: String): String =
    "${title.lowercase().trim()}:${year.trim()}"
