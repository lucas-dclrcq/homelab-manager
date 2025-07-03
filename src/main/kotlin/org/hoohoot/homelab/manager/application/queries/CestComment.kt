package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped

object CestComment : Query<String>

@Startup
@ApplicationScoped
class CestCommentQueryHandler : QueryHandler<CestComment, String> {
    private val deados = listOf("DEAAAADOOOO", "Dddddeeeeeaaaddoooo", "Deado", "deaaaaado", "C'est deaaaaaado", "Tellement deado", "C'est deado")

    override suspend fun handle(query: CestComment): String = deados.random()
}