package com.epicery.app.domain.model

/**
 * Resultado de [com.epicery.app.domain.calculator.BudgetCalculator.estimateWeeklyBudget]: el total
 * estimado de una [ShoppingList], su desglose por categoria alimenticia, la tendencia frente a
 * semanas anteriores y la proyeccion de gasto mensual (RF4, CA3).
 */
data class BudgetEstimate(
    val total: Double,
    val byCategory: Map<String, Double>,
    val trend: BudgetTrend,
    val monthlyProjection: Double
)

/** Tendencia del [BudgetEstimate.total] de esta semana contra el promedio de semanas anteriores. */
enum class BudgetTrend {
    INCREASING,
    DECREASING,
    STABLE
}
