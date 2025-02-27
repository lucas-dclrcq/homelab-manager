package org.hoohoot.homelab.manager.notifications.infrastructure.matrix

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import jakarta.ws.rs.ext.ContextResolver


class MatrixApiObjectMapper : ContextResolver<ObjectMapper> {
    override fun getContext(type: Class<*>?): ObjectMapper {
        val om = ObjectMapper()
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        om.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
        om.findAndRegisterModules()
        return om
    }
}