package org.hoohoot.homelab.manager.shared.http

import io.quarkus.vertx.http.runtime.filters.Filters
import io.vertx.ext.web.RoutingContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

/**
 * Forces `Cache-Control: no-cache` on the SPA HTML shell.
 *
 * Static resources (including `index.html`) are otherwise served with
 * `Cache-Control: public, immutable, max-age=86400`. That is correct for the
 * content-hashed bundles under `/assets`, but fatal for the HTML shell: when the
 * OIDC session expires, `/api/me` returns 401/499 and the frontend triggers a
 * full `window.location.reload()` (see webui `axios-instance.ts`) expecting the
 * server to answer the top-level navigation with a 302 to the IdP. With an
 * immutable cache, browsers (Firefox notably) re-serve `index.html` from disk
 * without ever contacting the server, so the redirect never happens and the SPA
 * loops on reload forever. Revalidating the shell keeps the re-auth flow working.
 */
@ApplicationScoped
class SpaCacheControlConfigurer {

    fun registerNoCacheForHtmlShell(@Observes filters: Filters) {
        filters.register({ rc: RoutingContext ->
            rc.addHeadersEndHandler {
                val contentType = rc.response().headers().get("Content-Type")
                if (contentType != null && contentType.startsWith("text/html")) {
                    rc.response().headers().set("Cache-Control", "no-cache")
                }
            }
            rc.next()
        }, HTML_NO_CACHE_FILTER_PRIORITY)
    }

    private companion object {
        // Runs before the static-resource handler so the headers-end hook is in place.
        const val HTML_NO_CACHE_FILTER_PRIORITY = 1000
    }
}
