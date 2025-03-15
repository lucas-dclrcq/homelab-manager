package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped

object Ping: Query<String>

@Startup
@ApplicationScoped
class PingQueryHandler : QueryHandler<Ping, String> {
    override suspend fun handle(query: Ping): String = "Pong!"
}