package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.LocalDate
import org.hoohoot.homelab.manager.application.ports.Calendar

enum class SkongType {
    Believer,
    Doubter
}

data class SkongResponse(val message: String)

data class GetSkong(val type: SkongType): Query<SkongResponse>

@Startup
@ApplicationScoped
class GetSkongQueryHandler(private val calendar: Calendar) : QueryHandler<GetSkong, SkongResponse> {
    private val skongOrigin = LocalDate.parse("2019-02-14")

    override suspend fun handle(query: GetSkong): SkongResponse {
        val daysSince = this.calendar.getDaysSince(skongOrigin)

        when (query.type) {
            SkongType.Doubter -> {
                """🟢 Doubt no more, my child."""
            }
            SkongType.Believer -> {
                """🟢 Patience, my child. Silksong will come when it is ready. The longer the wait, the greater the masterpiece!"""
            }
        }.let { return SkongResponse(it) }
    }
}
