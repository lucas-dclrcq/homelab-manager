package org.hoohoot.homelab.manager.statistics.domain.usecases

import io.quarkus.vertx.VertxContextSupport
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.ports.JellystatBackupReader
import org.hoohoot.homelab.manager.statistics.domain.ports.PlaybackSessions
import java.nio.file.Path
import java.util.concurrent.Callable

data class ImportResult(val imported: Int, val duplicates: Int, val ignored: Int)

@ApplicationScoped
class ImportJellystatBackup(
    private val reader: JellystatBackupReader,
    private val playbackSessions: PlaybackSessions,
) {
    suspend operator fun invoke(file: Path): ImportResult {
        // Lecture bloquante d'un gros fichier : déportée sur un worker Vertx (pas Dispatchers.IO,
        // dont les threads n'ont pas le classloader Quarkus)
        val content = VertxContextSupport.executeBlocking(Callable { reader.read(file) }).awaitSuspending()
        var imported = 0
        content.records.chunked(BATCH_SIZE).forEach { batch ->
            imported += playbackSessions.insertIgnoringDuplicates(batch)
        }
        return ImportResult(
            imported = imported,
            duplicates = content.records.size - imported,
            ignored = content.ignored,
        )
    }

    companion object {
        private const val BATCH_SIZE = 500
    }
}
