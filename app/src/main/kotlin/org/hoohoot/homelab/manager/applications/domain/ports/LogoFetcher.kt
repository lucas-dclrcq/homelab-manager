package org.hoohoot.homelab.manager.applications.domain.ports

import org.hoohoot.homelab.manager.applications.domain.LogoUpload

interface LogoFetcher {
    suspend fun fetch(url: String): LogoUpload?
}
