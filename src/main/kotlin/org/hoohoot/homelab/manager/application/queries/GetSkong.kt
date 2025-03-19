package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.*

enum class SkongType {
    Believer,
    Doubter
}

data class SkongResponse(val message: String)

data class GetSkong(val type: SkongType): Query<SkongResponse>

@Startup
@ApplicationScoped
class GetSkongQueryHandler : QueryHandler<GetSkong, SkongResponse> {
    private val skongOrigin = LocalDate.parse("2019-02-14")

    override suspend fun handle(query: GetSkong): SkongResponse {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val daysSince = skongOrigin.daysUntil(currentDate.date)

        when (query.type) {
            SkongType.Believer -> {
                "It's been <b>$daysSince days</b>, and there is still no release date. Face it, <i>Silksong</i> is never coming out. Team Cherry is just a myth."
            }
            SkongType.Doubter -> {
                "Patience, my child. <i>Silksong</i> will come when it is ready. The longer the wait, the greater the masterpiece!"
            }
        }.let { return SkongResponse(it) }
    }
}
