package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.GroceryItem
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica la semana de gasto (lunes a domingo, hora de Montreal) y que solo cuenten las
 * compras de la semana actual, con el precio pagado cuando existe y el estimado si no.
 */
class WeeklySpendingTest {

    private val zone: ZoneId = WeeklySpending.MONTREAL_ZONE

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    private fun item(
        name: String,
        price: Double,
        purchased: Boolean = true,
        purchasedAt: Long? = null,
        pricePaid: Double? = null
    ) = GroceryItem(
        name = name,
        foodGroup = "FRUITS",
        estimatedPrice = price,
        isPurchased = purchased,
        purchasedAt = purchasedAt,
        pricePaid = pricePaid
    )

    // 2026-10-21 es miercoles: su semana va del lunes 2026-10-19 al domingo 2026-10-25.
    private val wednesday = millis(2026, 10, 21)

    @Test
    fun `week starts on Monday at midnight Montreal time`() {
        assertEquals(millis(2026, 10, 19, 0), WeeklySpending.weekStartMillis(wednesday))
    }

    @Test
    fun `Sunday night still belongs to the same week`() {
        val sundayNight = millis(2026, 10, 25, 23, 59)
        assertEquals(millis(2026, 10, 19, 0), WeeklySpending.weekStartMillis(sundayNight))
    }

    @Test
    fun `Monday at midnight starts a new week`() {
        val nextMonday = millis(2026, 10, 26, 0, 0)
        assertEquals(nextMonday, WeeklySpending.weekStartMillis(nextMonday))
        assertEquals(nextMonday, WeeklySpending.nextWeekStartMillis(wednesday))
    }

    @Test
    fun `week boundaries follow the daylight saving change`() {
        // El 2026-11-01 se retrocede la hora: la semana del 2026-10-26 dura 7 dias + 1 hora.
        val start = WeeklySpending.weekStartMillis(millis(2026, 10, 28))
        val next = WeeklySpending.nextWeekStartMillis(millis(2026, 10, 28))
        assertEquals(millis(2026, 10, 26, 0), start)
        assertEquals(millis(2026, 11, 2, 0), next)
        assertEquals(7L * 24 * 3600 * 1000 + 3600 * 1000, next - start)
    }

    @Test
    fun `only items bought this week are counted`() {
        val items = listOf(
            item("Esta semana", 5.0, purchasedAt = millis(2026, 10, 20)),
            item("Semana pasada", 7.0, purchasedAt = millis(2026, 10, 18, 23, 59)),
            item("Proxima semana", 9.0, purchasedAt = millis(2026, 10, 26, 0, 0))
        )

        val result = WeeklySpending.purchasedThisWeek(items, wednesday)

        assertEquals(listOf("Esta semana"), result.map { it.name })
    }

    @Test
    fun `items not marked as purchased are never counted`() {
        val items = listOf(item("Pendiente", 5.0, purchased = false, purchasedAt = millis(2026, 10, 20)))

        assertTrue(WeeklySpending.purchasedThisWeek(items, wednesday).isEmpty())
    }

    @Test
    fun `purchased item without a date counts in the current week`() {
        val items = listOf(item("Dato antiguo", 5.0, purchasedAt = null))

        assertEquals(1, WeeklySpending.purchasedThisWeek(items, wednesday).size)
    }

    @Test
    fun `spent amount prefers the price paid over the estimate`() {
        assertEquals(4.0, item("Con precio real", 5.0, pricePaid = 4.0).spentAmount, 0.0001)
        assertEquals(5.0, item("Sin precio real", 5.0).spentAmount, 0.0001)
        assertEquals(0.0, item("Gratis", 5.0, pricePaid = 0.0).spentAmount, 0.0001)
    }
}
