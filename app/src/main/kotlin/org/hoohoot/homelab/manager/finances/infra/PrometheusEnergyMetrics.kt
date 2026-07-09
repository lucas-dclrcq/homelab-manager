package org.hoohoot.homelab.manager.finances.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.finances.domain.ports.EnergyMetrics
import org.hoohoot.homelab.manager.shared.prometheus.PrometheusRestClient
import java.time.Duration
import java.time.Instant

// Le max() agrège les séries dupliquées par le churn des pods snmp-exporter :
// increase(upsHighPrecOutputEnergyUsage) serait cassé à chaque restart du pod
private const val POWER_QUERY = "max(upsAdvOutputActivePower)"

@ApplicationScoped
class PrometheusEnergyMetrics(
    @param:RestClient private val prometheusRestClient: PrometheusRestClient,
) : EnergyMetrics {

    override suspend fun currentPowerWatts(): Double? =
        prometheusRestClient.query(POWER_QUERY, null).firstValue()

    override suspend fun averagePowerWatts(from: Instant, to: Instant): Double? {
        val seconds = Duration.between(from, to).seconds
        if (seconds <= 0) return null
        val query = "avg_over_time(($POWER_QUERY)[${seconds}s:5m])"
        return prometheusRestClient.query(query, to.epochSecond.toString()).firstValue()
    }
}
