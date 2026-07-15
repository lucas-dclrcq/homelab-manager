package org.hoohoot.homelab.manager.shared.vertx

import io.quarkus.logging.Log
import io.vertx.core.Vertx
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@ApplicationScoped
class BackgroundTasks(private val vertx: Vertx) {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("background-tasks"))

    fun launch(failureMessage: String, block: suspend () -> Unit): Job =
        scope.launch(
            newSafeVertxDispatcher(vertx) +
                QuarkusClassLoaderElement(Thread.currentThread().contextClassLoader)
        ) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.error(failureMessage, e)
            }
        }

    @PreDestroy
    fun shutdown() = scope.cancel()
}
