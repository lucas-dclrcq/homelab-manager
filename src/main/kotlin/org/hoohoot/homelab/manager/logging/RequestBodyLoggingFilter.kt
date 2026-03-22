package org.hoohoot.homelab.manager.logging

import io.quarkus.logging.Log
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.ext.Provider
import jakarta.ws.rs.ext.ReaderInterceptor
import jakarta.ws.rs.ext.ReaderInterceptorContext
import java.io.ByteArrayInputStream
import java.io.IOException

@Provider
class RequestBodyLoggingFilter : ReaderInterceptor {

    @Inject
    lateinit var config: RequestLoggingConfiguration

    @Throws(IOException::class, WebApplicationException::class)
    override fun aroundReadFrom(context: ReaderInterceptorContext): Any {
        if (!config.logBody()) {
            return context.proceed()
        }

        val body = context.inputStream.readAllBytes()
        val bodyString = String(body, Charsets.UTF_8)

        Log.infof("Incoming request body: %s", bodyString)

        context.inputStream = ByteArrayInputStream(body)
        return context.proceed()
    }
}
