package org.hoohoot.homelab.manager.statistics.infra.imports

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.jobs.ManagedJob
import org.hoohoot.homelab.manager.statistics.domain.usecases.ImportJellystatBackup
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Import du backup Jellystat, découplé de l'upload HTTP : l'endpoint admin stage le fichier
 * puis déclenche ce job en arrière-plan (le fichier fait des centaines de Mo, le parsing et
 * les insertions dépassent le budget d'une requête). Pas de @Scheduled : déclenchement
 * uniquement via runNow — relançable depuis l'UI admin jobs tant que le fichier stagé existe
 * (il n'est supprimé qu'en fin d'import réussi).
 */
@ApplicationScoped
class JellystatImportJob(
    private val importJellystatBackup: ImportJellystatBackup,
    @ConfigProperty(name = "statistics.import.staging-path") stagingPath: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Import backup Jellystat"
    override val schedule = "manuel (après upload)"

    private val stagedFile: Path = Path.of(stagingPath)
    private val running = AtomicBoolean(false)

    fun isRunning(): Boolean = running.get()

    fun stage(uploadedFile: Path) {
        stagedFile.parent?.let(Files::createDirectories)
        Files.move(uploadedFile, stagedFile, StandardCopyOption.REPLACE_EXISTING)
    }

    override suspend fun execute() {
        check(running.compareAndSet(false, true)) { "Un import Jellystat est déjà en cours" }
        try {
            check(Files.exists(stagedFile)) { "Aucun fichier de backup stagé ($stagedFile) : uploader un backup d'abord" }
            val result = importJellystatBackup(stagedFile)
            Log.info(
                "Import Jellystat terminé : ${result.imported} session(s) importée(s), " +
                    "${result.duplicates} doublon(s), ${result.ignored} entrée(s) ignorée(s)",
            )
            Files.deleteIfExists(stagedFile)
        } finally {
            running.set(false)
        }
    }

    companion object {
        const val IDENTITY = "jellystat-import"
    }
}
