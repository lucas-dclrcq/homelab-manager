package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.GifGateway

data class GetGif(val query: String) : Query<Gif>
data class Gif(val file: ByteArray, val width: Int = 0, val height: Int = 0)

@Startup
@ApplicationScoped
class GetGifQueryHandler(private val gifGateway: GifGateway) : QueryHandler<GetGif, Gif> {
    override suspend fun handle(query: GetGif): Gif = gifGateway.searchGif(query.query)
}