package org.hoohoot.homelab.manager.statistics.api

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.jobs.JobRunner
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.vertx.BackgroundTasks
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
    private val backgroundTasks: BackgroundTasks,
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
        
        backgroundTasks.launch("Échec de l'import Jellystat") {
            jobRunner.runNow(importJob)
        }
        return Response.accepted(ImportStartedDto(JellystatImportJob.IDENTITY)).build()
    }
}
