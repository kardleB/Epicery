package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.SpendingEntry
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Semana de gasto: de lunes 00:00 a domingo 23:59 en la zona horaria de Montreal. Es logica de
 * dominio pura (sin Android ni Room) para poder testearla como JVM test.
 */
object WeeklySpending {

    val MONTREAL_ZONE: ZoneId = ZoneId.of("America/Toronto")

    /** Lunes de la semana que contiene [date]. */
    fun mondayOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun dateOf(millis: Long, zone: ZoneId = MONTREAL_ZONE): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    /** Inicio (epoch millis) de la semana, lunes 00:00 local, que contiene [nowMillis]. */
    fun weekStartMillis(nowMillis: Long, zone: ZoneId = MONTREAL_ZONE): Long =
        mondayOf(dateOf(nowMillis, zone)).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Inicio (epoch millis) de la semana siguiente; limite exclusivo de la semana de [nowMillis]. */
    fun nextWeekStartMillis(nowMillis: Long, zone: ZoneId = MONTREAL_ZONE): Long =
        mondayOf(dateOf(nowMillis, zone)).plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Lineas de gasto de la semana de [nowMillis]. */
    fun inWeek(
        entries: List<SpendingEntry>,
        nowMillis: Long,
        zone: ZoneId = MONTREAL_ZONE
    ): List<SpendingEntry> {
        val start = weekStartMillis(nowMillis, zone)
        val end = nextWeekStartMillis(nowMillis, zone)
        return entries.filter { it.purchasedAt in start until end }
    }
}
