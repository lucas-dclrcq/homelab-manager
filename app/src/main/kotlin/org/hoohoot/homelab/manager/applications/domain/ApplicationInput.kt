package org.hoohoot.homelab.manager.applications.domain

val ALLOWED_LOGO_CONTENT_TYPES = setOf("image/png", "image/jpeg", "image/svg+xml", "image/webp")
const val MAX_LOGO_SIZE_BYTES = 1024L * 1024L

data class LogoUpload(val bytes: ByteArray, val contentType: String?)

sealed interface LogoChange {
    /** Formulaire admin sans fichier : le logo existant est conservé tel quel */
    data object Keep : LogoChange

    /** Upload manuel : remplace le logo et efface la source URL (le déclaratif reprendra la main au prochain drift) */
    data class Upload(val logo: LogoUpload) : LogoChange

    /** Source déclarée par l'opérateur : téléchargée seulement si l'URL diffère de celle déjà stockée */
    data class FromUrl(val url: String) : LogoChange

    /** Plus de source déclarée : supprime le logo seulement s'il provenait d'une URL (un upload manuel survit) */
    data object Remove : LogoChange
}

data class ApplicationInput(
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val managedBy: String? = null,
    val externalId: String? = null,
    val logo: LogoChange = LogoChange.Keep,
)
