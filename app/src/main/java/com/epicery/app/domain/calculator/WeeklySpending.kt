package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.GroceryItem
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Semana de gasto: de lunes 00:00 a domingo 23:59 en la zona horaria de Montreal. Es logica de
 * dominio pura (sin Android ni Room) para poder testearla como JVM test.
 */
object WeeklySpending {

    val MONTREAL_ZONE: ZoneId = ZoneId.of("America/Toronto")

    /** Inicio (epoch millis) de la semana, lunes 00:00 local, que contiene [nowMillis]. */
    fun weekStartMillis(nowMillis: Long, zone: ZoneId = MONTREAL_ZONE): Long =
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant().toEpochMilli()

    /** Inicio (epoch millis) de la semana siguiente; limite exclusivo de la semana de [nowMillis]. */
    fun nextWeekStartMillis(nowMillis: Long, zone: ZoneId = MONTREAL_ZONE): Long =
        Instant.ofEpochMilli(weekStartMillis(nowMillis, zone)).atZone(zone).toLocalDate()
            .plusWeeks(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()

    /**
     * Items comprados durante la semana de [nowMillis]. Un item marcado como comprado sin fecha
     * (dato antiguo) cuenta en la semana actual.
     */
    fun purchasedThisWeek(
        items: List<GroceryItem>,
        nowMillis: Long,
        zone: ZoneId = MONTREAL_ZONE
    ): List<GroceryItem> {
        val start = weekStartMillis(nowMillis, zone)
        val end = nextWeekStartMillis(nowMillis, zone)
        return items.filter { item ->
            item.isPurchased && (item.purchasedAt ?: nowMillis) in start until end
        }
    }
}
