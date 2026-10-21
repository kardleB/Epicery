package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.GroceryItem
import java.time.YearMonth
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica el mes calendario de Montreal, que compras cuentan en cada mes y el estado del presupuesto. */
class MonthlySpendingTest {

    private val zone = WeeklySpending.MONTREAL_ZONE
    private val october = YearMonth.of(2026, 10)

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    private fun item(name: String, price: Double, purchasedAt: Long?, purchased: Boolean = true) = GroceryItem(
        name = name,
        foodGroup = "FRUITS",
        estimatedPrice = price,
        isPurchased = purchased,
        purchasedAt = purchasedAt
    )

    @Test
    fun `month is read in Montreal time and not in UTC`() {
        // 2026-11-01 02:30 UTC todavia es 2026-10-31 22:30 en Montreal.
        val utcNovemberFirst = ZonedDateTime.of(2026, 11, 1, 2, 30, 0, 0, java.time.ZoneOffset.UTC)
            .toInstant().toEpochMilli()

        assertEquals(october, MonthlySpending.monthOf(utcNovemberFirst))
    }

    @Test
    fun `first and last instant of the month belong to that month`() {
        assertEquals(october, MonthlySpending.monthOf(millis(2026, 10, 1, 0, 0)))
        assertEquals(october, MonthlySpending.monthOf(millis(2026, 10, 31, 23, 59)))
        assertEquals(YearMonth.of(2026, 11), MonthlySpending.monthOf(millis(2026, 11, 1, 0, 0)))
    }

    @Test
    fun `only items bought in the month are counted`() {
        val now = millis(2026, 10, 21)
        val items = listOf(
            item("Octubre", 5.0, millis(2026, 10, 3)),
            item("Septiembre", 7.0, millis(2026, 9, 30, 23, 59)),
            item("Noviembre", 9.0, millis(2026, 11, 1, 0, 0)),
            item("Pendiente", 11.0, millis(2026, 10, 3), purchased = false)
        )

        val result = MonthlySpending.purchasedInMonth(items, october, now)

        assertEquals(listOf("Octubre"), result.map { it.name })
    }

    @Test
    fun `purchased item without a date counts in the current month`() {
        val now = millis(2026, 10, 21)

        assertEquals(1, MonthlySpending.purchasedInMonth(listOf(item("Antiguo", 5.0, null)), october, now).size)
        assertTrue(
            MonthlySpending.purchasedInMonth(listOf(item("Antiguo", 5.0, null)), YearMonth.of(2026, 9), now).isEmpty()
        )
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
