package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.Expense
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.model.SpendingEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Una semana de la lista "Gastos por semana". [start] y [end] son las fechas de la semana
 * (lunes a domingo) recortadas al mes mostrado: una semana que cruza de un mes a otro aparece en
 * cada mes solo con las compras de ese mes.
 */
data class WeekSpending(
    val start: LocalDate,
    val end: LocalDate,
    val entries: List<SpendingEntry>
) {
    val total: Double get() = entries.sumOf { it.amount }
}

object WeeklyExpenses {

    /**
     * Junta lo ya terminado ([expenses]) con lo recien marcado como comprado en la lista
     * ([items]); un item marcado sin fecha (dato antiguo) usa [nowMillis].
     */
    fun entries(items: List<GroceryItem>, expenses: List<Expense>, nowMillis: Long): List<SpendingEntry> =
        expenses.map { SpendingEntry(it.name, it.foodGroup, it.amount, it.purchasedAt, isArchived = true) } +
            items.filter { it.isPurchased }.map {
                SpendingEntry(it.name, it.foodGroup, it.spentAmount, it.purchasedAt ?: nowMillis, isArchived = false)
            }

    /** Semanas de [month] con compras, de la mas reciente a la mas antigua; cada una con lo mas reciente primero. */
    fun groupByWeek(
        entries: List<SpendingEntry>,
        month: YearMonth,
        zone: ZoneId = WeeklySpending.MONTREAL_ZONE
    ): List<WeekSpending> {
        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        return MonthlySpending.inMonth(entries, month, zone)
            .groupBy { WeeklySpending.mondayOf(WeeklySpending.dateOf(it.purchasedAt, zone)) }
            .map { (monday, weekEntries) ->
                WeekSpending(
                    start = maxOf(monday, firstDay),
                    end = minOf(monday.plusDays(6), lastDay),
                    entries = weekEntries.sortedByDescending { it.purchasedAt }
                )
            }
            .sortedByDescending { it.start }
    }
}
