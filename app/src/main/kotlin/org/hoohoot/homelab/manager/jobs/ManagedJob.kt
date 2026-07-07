package org.hoohoot.homelab.manager.jobs

/**
 * A scheduled job that can also be triggered manually from the admin UI.
 * [schedule] carries the human-readable planning expression because the
 * scheduler [io.quarkus.scheduler.Trigger] API does not expose it.
 */
interface ManagedJob {
    val identity: String
    val displayName: String
    val schedule: String

    suspend fun execute()
}
