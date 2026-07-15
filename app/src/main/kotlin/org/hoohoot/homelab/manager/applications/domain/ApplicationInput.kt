package org.hoohoot.homelab.manager.applications.domain

val ALLOWED_LOGO_CONTENT_TYPES = setOf("image/png", "image/jpeg", "image/svg+xml", "image/webp")
const val MAX_LOGO_SIZE_BYTES = 1024L * 1024L

data class LogoUpload(val bytes: ByteArray, val contentType: String?)

sealed interface LogoChange {
    data object Keep : LogoChange

    data class Upload(val logo: LogoUpload) : LogoChange

    data class FromUrl(val url: String) : LogoChange

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
