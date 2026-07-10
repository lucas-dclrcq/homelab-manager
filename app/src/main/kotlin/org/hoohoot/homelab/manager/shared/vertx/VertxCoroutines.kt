package org.hoohoot.homelab.manager.shared.vertx

import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle
import io.smallrye.common.vertx.VertxContext
import io.vertx.core.Context
import io.vertx.core.Vertx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class VertxContextDispatcher(private val context: Context) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.context.runOnContext { block.run() }
    }
}

/**
 * Exécute [block] sur un contexte Vertx duplé et marqué safe — prérequis d'Hibernate Reactive
 * Panache. Nécessaire pour le code appelé hors requête HTTP ou scheduler Quarkus (ex : commandes
 * du bot Matrix qui tournent sur les dispatchers trixnity).
 */
suspend fun <T> runOnSafeVertxContext(vertx: Vertx, block: suspend () -> T): T {
    val context = VertxContext.getOrCreateDuplicatedContext(vertx)
    VertxContextSafetyToggle.setContextSafe(context, true)
    return withContext(VertxContextDispatcher(context)) { block() }
}
