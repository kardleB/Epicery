package com.epicery.app.ui.budget

import com.epicery.app.data.local.FoodGroup

/**
 * Estado de la pantalla Budget (ver `docs/design/wireframes.md`, sección "4. Budget"). El gasto
 * semanal ([weeklySpent] y [spendingByCategory]) se calcula sobre los items ya marcados como
 * comprados de la lista activa, y se recalcula en tiempo real al observarla como `Flow` (RF4,
 * CA3). [monthlyProjection] extrapola ese gasto semanal a un mes completo usando
 * [WEEKS_PER_MONTH].
 */
data class BudgetUiState(
    val isLoading: Boolean = true,
    val weeklyBudget: Double = 0.0,
    val weeklySpent: Double = 0.0,
    val spendingByCategory: Map<FoodGroup, Double> = emptyMap(),
    val monthlyProjection: Double = 0.0
) {
    val progress: Float
        get() = if (weeklyBudget > 0) (weeklySpent / weeklyBudget).toFloat().coerceIn(0f, 1f) else 0f

    val isOverWeeklyBudget: Boolean get() = weeklyBudget > 0 && weeklySpent > weeklyBudget

    val monthlyBudget: Double get() = weeklyBudget * WEEKS_PER_MONTH

    val isOverMonthlyProjection: Boolean get() = monthlyBudget > 0 && monthlyProjection > monthlyBudget

    companion object {
        /** Semanas promedio por mes (365.25 días / 12 meses / 7 días), usada para proyectar el
         * gasto mensual a partir del gasto semanal acumulado. */
        const val WEEKS_PER_MONTH = 4.345
    }
}
