package org.hoohoot.homelab.manager.statistics.api

import io.quarkus.logging.Log
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle
import io.smallrye.common.vertx.VertxContext
import io.vertx.core.Vertx
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.jobs.JobRunner
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.vertx.VertxContextDispatcher
import org.hoohoot.homelab.manager.statistics.infra.imports.JellystatImportJob
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.multipart.FileUpload

data class ImportStartedDto(val jobIdentity: String)

@Path("/api/admin/statistics")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(name = "Statistics")
class AdminStatisticsResource(
    private val importJob: JellystatImportJob,
    private val jobRunner: JobRunner,
    private val vertx: Vertx,
) {

    /**
     * Stage le fichier de backup uploadé et déclenche l'import en arrière-plan (202) :
     * le fichier fait des centaines de Mo, l'import est trop long pour la requête.
     * Suivi via l'API admin jobs (identité "jellystat-import").
     */
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @APIResponseSchema(value = ImportStartedDto::class, responseCode = "202")
    fun importBackup(@RestForm("file") file: FileUpload?): Response {
        if (file == null || file.uploadedFile() == null) {
            return badRequest("Aucun fichier fourni")
        }
        if (importJob.isRunning()) {
            return conflict("Un import Jellystat est déjà en cours")
        }
        importJob.stage(file.uploadedFile())
        launchInBackground {
            jobRunner.runNow(importJob)
        }
        return Response.accepted(ImportStartedDto(JellystatImportJob.IDENTITY)).build()
    }

    // L'import survit à la requête : coroutine sur un contexte Vertx duplé et marqué safe,
    // condition pour que Hibernate Reactive accepte d'y ouvrir des sessions
    private fun launchInBackground(block: suspend () -> Unit) {
        val context = VertxContext.getOrCreateDuplicatedContext(vertx)
        VertxContextSafetyToggle.setContextSafe(context, true)
        CoroutineScope(SupervisorJob() + VertxContextDispatcher(context)).launch {
            try {
                block()
            } catch (e: Exception) {
                Log.error("Échec du lancement de l'import Jellystat", e)
            }
        }
    }
}
