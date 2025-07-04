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
        "DEAAAADOOOO" to 100,
        "Dddddeeeeeaaaddoooo" to 100,
        "Deado" to 100,
        "deaaaaado" to 100,
        "C'est deaaaaaado" to 100,
        "C'est deado" to 100,
        "IT IS DEEEEAADO" to 70,
        "DEADO !!!" to 70,
        "C'est complètement deado" to 70,
        "Totalement deado" to 70,
        "deado deado deado" to 70,
        "D-E-A-D-O" to 50,
        "Deado à mort" to 50,
        "Super deado" to 50,
        "Méga deado" to 50,
        "Ultra deado" to 50,
        "Deado forever" to 20,
        "RIP deado" to 20,
        "Archi deado" to 20,
        "Deado 💀" to 10,
        "Deado dans l'âme" to 10,
        "DEEEP DEEADOO" to 10,
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