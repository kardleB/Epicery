package com.epicery.app.ui.budget

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.calculator.MonthlyBudgetStatus
import com.epicery.app.domain.calculator.MonthlySpending
import java.time.YearMonth

/**
 * Estado de la pantalla Budget (ver `docs/design/wireframes.md`, sección "4. Budget"). El gasto
 * semanal ([weeklySpent] y [spendingByCategory]) se calcula sobre los items comprados esta semana
 * de la lista activa y se recalcula en tiempo real al observarla como `Flow` (RF4, CA3).
 *
 * El presupuesto del mes ([monthlyBudget], 0 si no esta definido) lo fija el usuario; el gasto del
 * mes ([monthlySpent]) suma lo comprado durante ese mes calendario. [monthlyProjection] extrapola el
 * gasto semanal a un mes completo usando [WEEKS_PER_MONTH]. [previousMonthlyBudget] es el del mes
 * anterior mas reciente (0 si no hay) para ofrecer reutilizarlo.
 */
data class BudgetUiState(
    val isLoading: Boolean = true,
    val weeklyBudget: Double = 0.0,
    val weeklySpent: Double = 0.0,
    val spendingByCategory: Map<FoodGroup, Double> = emptyMap(),
    val monthlyProjection: Double = 0.0,
    val month: YearMonth? = null,
    val monthlyBudget: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val previousMonthlyBudget: Double = 0.0
) {
    val progress: Float
        get() = if (weeklyBudget > 0) (weeklySpent / weeklyBudget).toFloat().coerceIn(0f, 1f) else 0f

    val isOverWeeklyBudget: Boolean get() = weeklyBudget > 0 && weeklySpent > weeklyBudget

    val isMonthlyBudgetSet: Boolean get() = monthlyBudget > 0

    val monthlyProgress: Float
        get() = if (isMonthlyBudgetSet) (monthlySpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f

    /** Porcentaje del presupuesto del mes ya gastado, redondeado; puede pasar de 100. */
    val monthlyPercentUsed: Int
        get() = if (isMonthlyBudgetSet) Math.round(monthlySpent / monthlyBudget * 100).toInt() else 0

    val monthlyRemaining: Double get() = monthlyBudget - monthlySpent

    val monthlyStatus: MonthlyBudgetStatus get() = MonthlySpending.status(monthlySpent, monthlyBudget)

    val isOverMonthlyProjection: Boolean get() = monthlyBudget > 0 && monthlyProjection > monthlyBudget

    /** Monto que se propone al definir el presupuesto del mes a partir del semanal (0 si no hay). */
    val suggestedMonthlyBudget: Double
        get() = if (weeklyBudget > 0) Math.round(weeklyBudget * WEEKS_PER_MONTH).toDouble() else 0.0

    companion object {
        /** Semanas promedio por mes (365.25 días / 12 meses / 7 días), usada para proyectar el
         * gasto mensual a partir del gasto semanal acumulado. */
        const val WEEKS_PER_MONTH = 4.345
    }
}
