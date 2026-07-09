package org.hoohoot.homelab.manager.applications.domain

data class LogoUpload(val bytes: ByteArray, val contentType: String?)

data class ApplicationInput(
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val managedBy: String? = null,
    val externalId: String? = null,
    val logo: LogoUpload? = null,
)
