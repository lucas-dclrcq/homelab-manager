package org.hoohoot.homelab.manager.finances.domain.ports

import java.time.Instant

interface EnergyMetrics {
    suspend fun currentPowerWatts(): Double?
    suspend fun averagePowerWatts(from: Instant, to: Instant): Double?
}
