package org.hoohoot.homelab.manager.applications.infra

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.applications.domain.ALLOWED_LOGO_CONTENT_TYPES
import org.hoohoot.homelab.manager.applications.domain.LogoUpload
import org.hoohoot.homelab.manager.applications.domain.MAX_LOGO_SIZE_BYTES
import org.hoohoot.homelab.manager.applications.domain.ports.LogoFetcher

@ApplicationScoped
class HttpLogoFetcher(@param:RestClient private val client: LogoDownloadClient) : LogoFetcher {

    override suspend fun fetch(url: String): LogoUpload? {
        val response = try {
            client.download(url)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.warn("Logo download failed for $url: ${e.message}")
            return null
        }

        if (response.status !in 200..299) {
            Log.warn("Logo download failed for $url: HTTP ${response.status}")
            return null
        }

        val contentType = response.mediaType?.let { "${it.type}/${it.subtype}" }
        if (contentType !in ALLOWED_LOGO_CONTENT_TYPES) {
            Log.warn("Logo download rejected for $url: content type $contentType not in $ALLOWED_LOGO_CONTENT_TYPES")
            return null
        }

        val bytes = response.entity
        if (bytes.size > MAX_LOGO_SIZE_BYTES) {
            Log.warn("Logo download rejected for $url: ${bytes.size} bytes exceeds $MAX_LOGO_SIZE_BYTES")
            return null
        }

        return LogoUpload(bytes, contentType)
    }
}
