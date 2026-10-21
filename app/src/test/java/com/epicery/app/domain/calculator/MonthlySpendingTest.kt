package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.SpendingEntry
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifica el mes calendario de Montreal, que compras cuentan en cada mes y el estado del presupuesto. */
class MonthlySpendingTest {

    private val zone = WeeklySpending.MONTREAL_ZONE
    private val october = YearMonth.of(2026, 10)

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    private fun entry(name: String, amount: Double, purchasedAt: Long) =
        SpendingEntry(name, "FRUITS", amount, purchasedAt, isArchived = false)

    @Test
    fun `month is read in Montreal time and not in UTC`() {
        // 2026-11-01 02:30 UTC todavia es 2026-10-31 22:30 en Montreal.
        val utcNovemberFirst = ZonedDateTime.of(2026, 11, 1, 2, 30, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals(october, MonthlySpending.monthOf(utcNovemberFirst))
    }

    @Test
    fun `first and last instant of the month belong to that month`() {
        assertEquals(october, MonthlySpending.monthOf(millis(2026, 10, 1, 0, 0)))
        assertEquals(october, MonthlySpending.monthOf(millis(2026, 10, 31, 23, 59)))
        assertEquals(YearMonth.of(2026, 11), MonthlySpending.monthOf(millis(2026, 11, 1, 0, 0)))
    }

    @Test
    fun `only entries bought in the month are counted`() {
        val entries = listOf(
            entry("Octubre", 5.0, millis(2026, 10, 3)),
            entry("Septiembre", 7.0, millis(2026, 9, 30, 23, 59)),
            entry("Noviembre", 9.0, millis(2026, 11, 1, 0, 0))
        )

        assertEquals(listOf("Octubre"), MonthlySpending.inMonth(entries, october).map { it.name })
    }

    @Test
    fun `status is not set when there is no budget`() {
        assertEquals(MonthlyBudgetStatus.NOT_SET, MonthlySpending.status(spent = 50.0, budget = 0.0))
    }

    @Test
    fun `status moves from on track to warning to over`() {
        assertEquals(MonthlyBudgetStatus.ON_TRACK, MonthlySpending.status(spent = 79.99, budget = 100.0))
        assertEquals(MonthlyBudgetStatus.WARNING, MonthlySpending.status(spent = 80.0, budget = 100.0))
        assertEquals(MonthlyBudgetStatus.WARNING, MonthlySpending.status(spent = 100.0, budget = 100.0))
        assertEquals(MonthlyBudgetStatus.OVER, MonthlySpending.status(spent = 100.01, budget = 100.0))
    }
}
