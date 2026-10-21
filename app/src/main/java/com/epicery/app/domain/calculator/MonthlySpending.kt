package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.SpendingEntry
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Estado del gasto del mes frente a su presupuesto. */
enum class MonthlyBudgetStatus {
    /** El usuario no definio presupuesto para el mes. */
    NOT_SET,
    ON_TRACK,

    /** Se uso el 80 % o mas del presupuesto, sin pasarse. */
    WARNING,
    OVER
}

/**
 * Gasto del mes calendario en la zona horaria de Montreal. Logica de dominio pura, sin Android ni
 * Room, para testearla como JVM test.
 */
object MonthlySpending {

    /** Fraccion del presupuesto a partir de la cual se avisa. */
    const val WARNING_RATIO = 0.8

    fun monthOf(millis: Long, zone: ZoneId = WeeklySpending.MONTREAL_ZONE): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(millis).atZone(zone))

    /** Lineas de gasto compradas en [month], segun su fecha de compra. */
    fun inMonth(
        entries: List<SpendingEntry>,
        month: YearMonth,
        zone: ZoneId = WeeklySpending.MONTREAL_ZONE
    ): List<SpendingEntry> = entries.filter { monthOf(it.purchasedAt, zone) == month }

    fun status(spent: Double, budget: Double): MonthlyBudgetStatus = when {
        budget <= 0.0 -> MonthlyBudgetStatus.NOT_SET
        spent > budget -> MonthlyBudgetStatus.OVER
        spent >= budget * WARNING_RATIO -> MonthlyBudgetStatus.WARNING
        else -> MonthlyBudgetStatus.ON_TRACK
    }
}
