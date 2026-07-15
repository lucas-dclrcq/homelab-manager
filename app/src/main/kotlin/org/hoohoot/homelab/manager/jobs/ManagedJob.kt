package org.hoohoot.homelab.manager.jobs

interface ManagedJob {
    val identity: String
    val displayName: String
    val schedule: String

    suspend fun execute()
}
