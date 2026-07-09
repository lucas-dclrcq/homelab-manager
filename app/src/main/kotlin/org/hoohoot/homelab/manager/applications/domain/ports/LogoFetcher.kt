package org.hoohoot.homelab.manager.applications.domain.ports

import org.hoohoot.homelab.manager.applications.domain.LogoUpload

interface LogoFetcher {
    /** null si le téléchargement échoue ou si l'image est invalide (type/taille) — loggé côté adapter */
    suspend fun fetch(url: String): LogoUpload?
}
