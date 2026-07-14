package org.hoohoot.homelab.manager.shared.arr.radarr

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.shared.arr.ArrTag
import org.hoohoot.homelab.manager.shared.arr.DiskSpace

@Path("/api/v3")
@RegisterRestClient(configKey = "radarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-Api-Key", value = ["\${radarr.api_key}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface RadarrRestClient {
    @GET
    @Path("/calendar")
    suspend fun getCalendar(
        @QueryParam("start") start: String?,
        @QueryParam("end") end: String?,
    ): List<RadarrMovie>?

    @GET
    @Path("/movie")
    suspend fun getMovies(): List<RadarrMovie>?

    @GET
    @Path("/history/since")
    suspend fun getHistorySince(
        @QueryParam("date") date: String,
        @QueryParam("includeMovie") includeMovie: Boolean,
    ): List<RadarrHistoryRecord>?

    @GET
    @Path("/diskspace")
    suspend fun getDiskSpace(): List<DiskSpace>?

    @GET
    @Path("/qualityprofile")
    suspend fun getQualityProfiles(): List<RadarrQualityProfile>?

    @GET
    @Path("/tag")
    suspend fun getTags(): List<ArrTag>?

    // Suppression idempotente : un 404 au retry signifie déjà supprimé, le @Retry de classe reste acceptable
    @DELETE
    @Path("/movie/{id}")
    suspend fun deleteMovie(
        @PathParam("id") id: Int,
        @QueryParam("deleteFiles") deleteFiles: Boolean,
        @QueryParam("addImportExclusion") addImportExclusion: Boolean,
    )

    // La recherche interactive interroge tous les indexers : doit dépasser le read-timeout de 120s
    @GET
    @Path("/release")
    @Timeout(value = 125, unit = ChronoUnit.SECONDS)
    suspend fun searchReleases(@QueryParam("movieId") movieId: Int): List<RadarrRelease>?

    // Seul appel non-idempotent : un grab rejoué déclencherait un double téléchargement
    @POST
    @Path("/release")
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 0)
    suspend fun grabRelease(request: RadarrGrabRequest): RadarrRelease?
}

suspend fun RadarrRestClient.getMovieCalendar(start: Instant, end: Instant): List<RadarrMovie> =
    getCalendar(
        start.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        end.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    ) ?: emptyList()
