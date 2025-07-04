package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped

object CestDeado : Query<String>

@Startup
@ApplicationScoped
class DeadoQueryHandler : QueryHandler<CestDeado, String> {
    private val deados = listOf(
        "DEAAAADOOOO",
        "DEAAAADOOOO",
        "DEAAAADOOOO",
        "DEAAAADOOOO",
        "DEAAAADOOOO",
        "Dddddeeeeeaaaddoooo",
        "Dddddeeeeeaaaddoooo",
        "Dddddeeeeeaaaddoooo",
        "Dddddeeeeeaaaddoooo",
        "Dddddeeeeeaaaddoooo",
        "Deado",
        "Deado",
        "Deado",
        "Deado",
        "Deado",
        "deaaaaado",
        "deaaaaado",
        "deaaaaado",
        "deaaaaado",
        "deaaaaado",
        "C'est deaaaaaado",
        "C'est deaaaaaado",
        "C'est deaaaaaado",
        "C'est deaaaaaado",
        "C'est deado",
        "C'est deado",
        "C'est deado",
        "C'est deado",
        "IT IS DEEEEAADO",
        "IT IS DEEEEAADO",
        "IT IS DEEEEAADO",
        "IT IS DEEEEAADO",
        "DEADO !!!",
        "DEADO !!!",
        "DEADO !!!",
        "C'est complètement deado",
        "C'est complètement deado",
        "C'est complètement deado",
        "Totalement deado",
        "Totalement deado",
        "Totalement deado",
        "deado deado deado",
        "deado deado deado",
        "deado deado deado",
        "D-E-A-D-O",
        "D-E-A-D-O",
        "D-E-A-D-O",
        "Deado à mort",
        "Deado à mort",
        "Deado à mort",
        "Super deado",
        "Super deado",
        "Super deado",
        "Méga deado",
        "Méga deado",
        "Méga deado",
        "Ultra deado",
        "Ultra deado",
        "Ultra deado",
        "Deado forever",
        "Deado forever",
        "Deado forever",
        "RIP deado",
        "RIP deado",
        "RIP deado",
        "Archi deado",
        "Archi deado",
        "Archi deado",
        "Deado 💀",
        "Deado dans l'âme",
        "DEEEP DEEADOO",
        "FIYAH DEADO 🔥🔥🔥",
        "DEADO INNA CHEMINAY",
        "DEADOCLAAT ❤️💛💚",
        "BRAINDEADO",
        "DEADOSAMAAAA (˶˃ ᵕ ˂˶)\uD83D\uDC9E",
        "ÉLÉMENTAIREMENT DEADO"
    )

    override suspend fun handle(query: CestDeado): String = deados.random()
}