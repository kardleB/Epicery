package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.GroceryItem
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

    fun monthOf(nowMillis: Long, zone: ZoneId = WeeklySpending.MONTREAL_ZONE): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(nowMillis).atZone(zone))

    /**
     * Items comprados en [month]. Un item marcado como comprado sin fecha (dato antiguo) cuenta en
     * el mes de [nowMillis].
     */
    fun purchasedInMonth(
        items: List<GroceryItem>,
        month: YearMonth,
        nowMillis: Long,
        zone: ZoneId = WeeklySpending.MONTREAL_ZONE
    ): List<GroceryItem> = items.filter { item ->
        item.isPurchased && monthOf(item.purchasedAt ?: nowMillis, zone) == month
    }

    fun status(spent: Double, budget: Double): MonthlyBudgetStatus = when {
        budget <= 0.0 -> MonthlyBudgetStatus.NOT_SET
        spent > budget -> MonthlyBudgetStatus.OVER
        spent >= budget * WARNING_RATIO -> MonthlyBudgetStatus.WARNING
        else -> MonthlyBudgetStatus.ON_TRACK
    }
}
