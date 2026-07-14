package org.hoohoot.homelab.manager.finances.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.ports.EntriesPage
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.finances.domain.ports.MonthlyTotals
import org.hoohoot.homelab.manager.finances.domain.ports.YearlySummary
import java.time.LocalDate
import java.util.UUID

data class FinanceEntryView(
    val id: UUID,
    val type: EntryType,
    val source: EntrySource,
    val label: String,
    val vendor: String?,
    val amountCents: Int,
    val entryDate: LocalDate,
    val memberId: UUID?,
    val memberDisplayName: String?,
    val period: String?,
    val notes: String?,
)

@ApplicationScoped
class FinanceEntryRepository : FinanceEntries {

    override suspend fun search(year: Int, type: EntryType?, page: Int, pageSize: Int): EntriesPage {
        val typeFilter = if (type != null) " and e.type = :type" else ""
        val items = Panache.withSession {
            Panache.getSession().flatMap { session ->
                val query = session.createQuery(
                    """select new org.hoohoot.homelab.manager.finances.infra.FinanceEntryView(
                           e.id, e.type, e.source, e.label, e.vendor, e.amountCents, e.entryDate,
                           e.memberId, m.displayName, e.period, e.notes)
                       from FinanceEntryEntity e
                       left join MemberEntity m on m.id = e.memberId
                       where extract(year from e.entryDate) = :year$typeFilter
                       order by e.entryDate desc, e.createdAt desc""",
                    FinanceEntryView::class.java
                )
                query.setParameter("year", year)
                type?.let { query.setParameter("type", it) }
                query.setFirstResult(page * pageSize).setMaxResults(pageSize).resultList
            }
        }.awaitSuspending()

        val total = Panache.withSession {
            Panache.getSession().flatMap { session ->
                val query = session.createQuery(
                    "select count(e) from FinanceEntryEntity e where extract(year from e.entryDate) = :year$typeFilter",
                    Long::class.java
                )
                query.setParameter("year", year)
                type?.let { query.setParameter("type", it) }
                query.singleResult
            }
        }.awaitSuspending()

        return EntriesPage(items, total.toLong(), page, pageSize)
    }

    override suspend fun yearlySummary(year: Int): YearlySummary {
        val rows = totalsByType(year)
        return YearlySummary(
            contributionsCents = rows[EntryType.CONTRIBUTION] ?: 0L,
            expensesCents = rows[EntryType.EXPENSE] ?: 0L,
        )
    }

    private suspend fun totalsByType(year: Int): Map<EntryType, Long> =
        Panache.withSession {
            Panache.getSession().flatMap { session ->
                session.createQuery(
                    """select e.type, sum(e.amountCents) from FinanceEntryEntity e
                       where extract(year from e.entryDate) = :year group by e.type""",
                    Array<Any>::class.java
                ).setParameter("year", year).resultList
            }
        }.awaitSuspending().associate { row -> row[0] as EntryType to (row[1] as Number).toLong() }

    override suspend fun monthlyBreakdown(year: Int): List<MonthlyTotals> {
        val rows = Panache.withSession {
            Panache.getSession().flatMap { session ->
                session.createQuery(
                    """select extract(month from e.entryDate), e.type, sum(e.amountCents)
                       from FinanceEntryEntity e
                       where extract(year from e.entryDate) = :year
                       group by extract(month from e.entryDate), e.type""",
                    Array<Any>::class.java
                ).setParameter("year", year).resultList
            }
        }.awaitSuspending()

        return rows
            .groupBy { row -> (row[0] as Number).toInt() }
            .map { (month, byType) ->
                val totals = byType.associate { row -> row[1] as EntryType to (row[2] as Number).toLong() }
                MonthlyTotals(
                    month = month,
                    contributionsCents = totals[EntryType.CONTRIBUTION] ?: 0L,
                    expensesCents = totals[EntryType.EXPENSE] ?: 0L,
                )
            }
            .sortedBy { it.month }
    }

    override suspend fun findById(id: UUID): FinanceEntryEntity? =
        Panache.withSession {
            FinanceEntryEntity.findById(id)
        }.awaitSuspending()

    override suspend fun save(entity: FinanceEntryEntity): FinanceEntryEntity =
        Panache.withTransaction {
            entity.persist<FinanceEntryEntity>()
        }.awaitSuspending()

    // Check-then-insert dans une même transaction : l'index unique (rule_id, period) /
    // (period, source=ENERGY) reste le garde-fou en cas de course entre run planifié et manuel
    override suspend fun saveIfAbsent(entity: FinanceEntryEntity): Boolean =
        Panache.withTransaction {
            val existing = when {
                entity.ruleId != null ->
                    FinanceEntryEntity.count("ruleId = ?1 and period = ?2", entity.ruleId!!, entity.period!!)
                else ->
                    FinanceEntryEntity.count("source = ?1 and period = ?2", entity.source, entity.period!!)
            }
            existing.flatMap { count ->
                if (count > 0L) {
                    io.smallrye.mutiny.Uni.createFrom().item(false)
                } else {
                    entity.persist<FinanceEntryEntity>().map { true }
                }
            }
        }.awaitSuspending()

    override suspend fun update(id: UUID, mutate: (FinanceEntryEntity) -> Unit): FinanceEntryEntity? =
        Panache.withTransaction {
            FinanceEntryEntity.findById(id).invoke { entity -> entity?.let(mutate) }
        }.awaitSuspending()

    override suspend fun delete(id: UUID): Boolean =
        Panache.withTransaction {
            FinanceEntryEntity.deleteById(id)
        }.awaitSuspending()

    override suspend fun energyPeriodExists(period: String): Boolean =
        Panache.withSession {
            FinanceEntryEntity.count("source = ?1 and period = ?2", EntrySource.ENERGY, period)
        }.awaitSuspending() > 0L
}
