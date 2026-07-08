package org.hoohoot.homelab.manager.shared.api

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

internal fun badRequest(message: String): Response =
    Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to message)).build()

internal fun conflict(message: String): Response =
    Response.status(Response.Status.CONFLICT).entity(mapOf("error" to message)).build()

internal fun notFound(): Response = Response.status(Response.Status.NOT_FOUND).build()

internal fun notFound(message: String): Response =
    Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to message)).build()

internal fun badRequestException(message: String) = WebApplicationException(badRequest(message))

internal fun conflictException(message: String) = WebApplicationException(conflict(message))

internal fun badGatewayException(message: String) = WebApplicationException(
    Response.status(Response.Status.BAD_GATEWAY).entity(mapOf("error" to message)).build(),
)
