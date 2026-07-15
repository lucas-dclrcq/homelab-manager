package org.hoohoot.homelab.manager.shared.vertx

import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle
import io.smallrye.common.vertx.VertxContext
import io.vertx.core.Context
import io.vertx.core.Vertx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

class VertxContextDispatcher(private val context: Context) : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        Vertx.currentContext() !== this.context

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.context.runOnContext { block.run() }
    }
}

fun newSafeVertxContext(vertx: Vertx): Context {
    val duplicated = VertxContext.createNewDuplicatedContext(vertx.getOrCreateContext())
    VertxContextSafetyToggle.setContextSafe(duplicated, true)
    return duplicated
}

fun newSafeVertxDispatcher(vertx: Vertx): CoroutineDispatcher =
    VertxContextDispatcher(newSafeVertxContext(vertx))


class QuarkusClassLoaderElement(
    private val classLoader: ClassLoader
) : ThreadContextElement<ClassLoader> {
    companion object Key : CoroutineContext.Key<QuarkusClassLoaderElement>

    override val key: CoroutineContext.Key<QuarkusClassLoaderElement> = Key

    override fun updateThreadContext(context: CoroutineContext): ClassLoader {
        val old = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
        return old
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: ClassLoader) {
        Thread.currentThread().contextClassLoader = oldState
    }
}
