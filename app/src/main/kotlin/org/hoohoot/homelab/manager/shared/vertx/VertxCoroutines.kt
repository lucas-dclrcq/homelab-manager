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

// Hibernate Reactive Panache exige un contexte Vertx duplé et marqué safe hors requête HTTP/scheduler
suspend fun <T> runOnSafeVertxContext(vertx: Vertx, block: suspend () -> T): T {
    val context = VertxContext.getOrCreateDuplicatedContext(vertx)
    VertxContextSafetyToggle.setContextSafe(context, true)
    return withContext(VertxContextDispatcher(context)) { block() }
}
