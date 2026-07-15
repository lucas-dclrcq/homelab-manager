package org.hoohoot.homelab.manager.cleanup.domain

import java.text.Normalizer

object Titles {
    private val diacritics = Regex("\\p{M}")
    private val nonAlphanumeric = Regex("[^a-z0-9]+")

    fun normalize(title: String): String =
        Normalizer.normalize(title.lowercase().trim(), Normalizer.Form.NFD)
            .replace(diacritics, "")
            .replace(nonAlphanumeric, " ")
            .trim()
}
