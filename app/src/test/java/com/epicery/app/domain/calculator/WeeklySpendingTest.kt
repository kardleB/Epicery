package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.SpendingEntry
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica la semana de gasto (lunes a domingo, hora de Montreal) y que solo cuenten las compras de la semana actual. */
class WeeklySpendingTest {

    private val zone: ZoneId = WeeklySpending.MONTREAL_ZONE

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    private fun entry(name: String, amount: Double, purchasedAt: Long, archived: Boolean = false) =
        SpendingEntry(name, "FRUITS", amount, purchasedAt, archived)

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
    fun `only entries bought this week are counted, archived or not`() {
        val entries = listOf(
            entry("Esta semana", 5.0, millis(2026, 10, 20)),
            entry("Archivada esta semana", 6.0, millis(2026, 10, 19, 0, 0), archived = true),
            entry("Semana pasada", 7.0, millis(2026, 10, 18, 23, 59)),
            entry("Proxima semana", 9.0, millis(2026, 10, 26, 0, 0))
        )

        val result = WeeklySpending.inWeek(entries, wednesday)

        assertEquals(listOf("Esta semana", "Archivada esta semana"), result.map { it.name })
    }

    @Test
    fun `no entries means nothing this week`() {
        assertTrue(WeeklySpending.inWeek(emptyList(), wednesday).isEmpty())
    }
}
