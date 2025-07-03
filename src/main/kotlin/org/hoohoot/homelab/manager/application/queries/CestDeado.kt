package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped

object CestDeado : Query<String>

@Startup
@ApplicationScoped
class DeadoQueryHandler : QueryHandler<CestDeado, String> {
    private val deados =
        listOf("DEAAAADOOOO", "Dddddeeeeeaaaddoooo", "Deado", "deaaaaado", "C'est deaaaaaado", "C'est deado")

    override suspend fun handle(query: CestDeado): String = deados.random()
}