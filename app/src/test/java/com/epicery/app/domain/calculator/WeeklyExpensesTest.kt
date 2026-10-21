package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.Expense
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.model.SpendingEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica como se juntan las compras terminadas con las marcadas y como se agrupan por semana de cada mes. */
class WeeklyExpensesTest {

    private val zone = WeeklySpending.MONTREAL_ZONE
    private val october = YearMonth.of(2026, 10)

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        ZonedDateTime.of(year, month, day, hour, 0, 0, 0, zone).toInstant().toEpochMilli()

    private fun entry(name: String, amount: Double, purchasedAt: Long) =
        SpendingEntry(name, "FRUITS", amount, purchasedAt, isArchived = true)

    @Test
    fun `entries join finished purchases with items checked in the list`() {
        val now = millis(2026, 10, 21)
        val expenses = listOf(Expense(1, "Leche", "DAIRY", 4.5, millis(2026, 10, 5)))
        val items = listOf(
            GroceryItem(1, "Manzanas", "FRUITS", 5.0, isPurchased = true, purchasedAt = millis(2026, 10, 20), pricePaid = 4.0),
            GroceryItem(2, "Pollo", "PROTEIN", 12.0, isPurchased = false),
            GroceryItem(3, "Antiguo", "FRUITS", 3.0, isPurchased = true, purchasedAt = null)
        )

        val result = WeeklyExpenses.entries(items, expenses, now)

        assertEquals(listOf("Leche", "Manzanas", "Antiguo"), result.map { it.name })
        assertEquals(listOf(true, false, false), result.map { it.isArchived })
        assertEquals(4.0, result[1].amount, 0.0001) // precio pagado en lugar del estimado
        assertEquals(now, result[2].purchasedAt) // sin fecha: usa el momento actual
    }

    @Test
    fun `groups the month by week from the most recent, with subtotals`() {
        val entries = listOf(
            entry("A", 10.0, millis(2026, 10, 7)),   // semana del 5 al 11
            entry("B", 20.0, millis(2026, 10, 9)),   // misma semana
            entry("C", 5.0, millis(2026, 10, 21)),   // semana del 19 al 25
            entry("D", 8.0, millis(2026, 10, 14))    // semana del 12 al 18
        )

        val weeks = WeeklyExpenses.groupByWeek(entries, october)

        assertEquals(
            listOf(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 5)),
            weeks.map { it.start }
        )
        assertEquals(listOf(5.0, 8.0, 30.0), weeks.map { it.total })
        assertEquals(listOf("B", "A"), weeks.last().entries.map { it.name }) // lo mas reciente primero
    }

    @Test
    fun `weeks are clipped to the month and keep only that month's purchases`() {
        // La semana del lunes 2026-09-28 al domingo 2026-10-04 cruza de septiembre a octubre.
        val entries = listOf(
            entry("Septiembre", 10.0, millis(2026, 9, 29)),
            entry("Octubre", 7.0, millis(2026, 10, 2))
        )

        val october = WeeklyExpenses.groupByWeek(entries, YearMonth.of(2026, 10)).single()
        val september = WeeklyExpenses.groupByWeek(entries, YearMonth.of(2026, 9)).single()

        assertEquals(LocalDate.of(2026, 10, 1), october.start)
        assertEquals(LocalDate.of(2026, 10, 4), october.end)
        assertEquals(7.0, october.total, 0.0001)
        assertEquals(LocalDate.of(2026, 9, 28), september.start)
        assertEquals(LocalDate.of(2026, 9, 30), september.end)
        assertEquals(10.0, september.total, 0.0001)
    }

    @Test
    fun `the last week of the month ends on the last day of the month`() {
        val week = WeeklyExpenses.groupByWeek(listOf(entry("X", 1.0, millis(2026, 10, 30))), october).single()

        assertEquals(LocalDate.of(2026, 10, 26), week.start)
        assertEquals(LocalDate.of(2026, 10, 31), week.end)
    }

    @Test
    fun `a month without purchases has no weeks`() {
        assertTrue(WeeklyExpenses.groupByWeek(listOf(entry("X", 1.0, millis(2026, 9, 15))), october).isEmpty())
    }
}
