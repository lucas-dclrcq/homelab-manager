package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import kotlin.random.Random

object CestDeado : Query<String>

@Startup
@ApplicationScoped
class DeadoQueryHandler : QueryHandler<CestDeado, String> {
    private val deadosWithWeights = listOf(
        "DEAAAADOOOO" to 5,
        "Dddddeeeeeaaaddoooo" to 5,
        "Deado" to 5,
        "deaaaaado" to 5,
        "C'est deaaaaaado" to 4,
        "C'est deado" to 4,
        "IT IS DEEEEAADO" to 4,
        "DEADO !!!" to 3,
        "C'est complètement deado" to 3,
        "Totalement deado" to 3,
        "deado deado deado" to 3,
        "D-E-A-D-O" to 3,
        "Deado à mort" to 3,
        "Super deado" to 3,
        "Méga deado" to 3,
        "Ultra deado" to 3,
        "Deado forever" to 3,
        "RIP deado" to 3,
        "Archi deado" to 3,
        "Deado 💀" to 2,
        "Deado dans l'âme" to 1,
        "DEEEP DEEADOO" to 1,
        "FIYAH DEADO 🔥🔥🔥" to 1,
        "DEADO INNA CHEMINAY" to 1,
        "DEADOCLAAT ❤️💛💚" to 1,
        "BRAINDEADO" to 1,
        "DEADOSAMAAAA (˶˃ ᵕ ˂˶)💞" to 1,
        "ÉLÉMENTAIREMENT DEADO" to 1
    )

    private val totalWeight = deadosWithWeights.sumOf { it.second }

    override suspend fun handle(query: CestDeado): String {
        val randomValue = Random.nextInt(1, totalWeight + 1)
        var currentWeight = 0

        for ((deado, weight) in deadosWithWeights) {
            currentWeight += weight
            if (randomValue <= currentWeight) {
                return deado
            }
        }

        return deadosWithWeights.first().first
    }
}