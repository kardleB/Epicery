package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.BudgetEstimate
import com.epicery.app.domain.model.BudgetTrend
import com.epicery.app.domain.model.PriceAlert
import com.epicery.app.domain.model.ShoppingList

/**
 * Calcula el presupuesto estimado de una [ShoppingList] a partir de los precios estimados de sus
 * items (RF4, CA3): el total, el desglose de gasto por categoria alimenticia (`GroceryItem.foodGroup`),
 * la tendencia contra el promedio de [previousWeeklyTotals] y la proyeccion de gasto mensual.
 *
 * Es una clase de dominio pura (sin dependencias de Android ni de Room) para poder testearla como
 * JVM test y reutilizarla tanto desde use cases como desde ViewModels.
 */
class BudgetCalculator {

    fun estimateWeeklyBudget(
        shoppingList: ShoppingList,
        previousWeeklyTotals: List<Double> = emptyList()
    ): BudgetEstimate {
        val total = shoppingList.items.sumOf { it.estimatedPrice }
        val byCategory = shoppingList.items
            .groupBy { it.foodGroup }
            .mapValues { (_, items) -> items.sumOf { it.estimatedPrice } }

        return BudgetEstimate(
            total = total,
            byCategory = byCategory,
            trend = calculateTrend(total, previousWeeklyTotals),
            monthlyProjection = total * MONTHLY_PROJECTION_FACTOR
        )
    }

    /**
     * Genera una [PriceAlert] cuando [currentPrice] supera en mas del 15%
     * ([PRICE_ALERT_THRESHOLD_RATIO]) el promedio de [historicalPrices] de un articulo (RF3, CA2).
     * Devuelve `null` si no hay historico, si el promedio es cero o negativo, o si el incremento
     * no supera el umbral.
     */
    fun alertIfOverAverage(itemName: String, currentPrice: Double, historicalPrices: List<Double>): PriceAlert? {
        if (historicalPrices.isEmpty()) return null

        val averagePrice = historicalPrices.average()
        if (averagePrice <= 0.0) return null

        val increaseRatio = (currentPrice - averagePrice) / averagePrice
        if (increaseRatio <= PRICE_ALERT_THRESHOLD_RATIO) return null

        return PriceAlert(
            itemName = itemName,
            currentPrice = currentPrice,
            averagePrice = averagePrice,
            increaseRatio = increaseRatio
        )
    }

    private fun calculateTrend(total: Double, previousWeeklyTotals: List<Double>): BudgetTrend {
        if (previousWeeklyTotals.isEmpty()) return BudgetTrend.STABLE

        val previousAverage = previousWeeklyTotals.average()
        if (previousAverage == 0.0) {
            return if (total > 0.0) BudgetTrend.INCREASING else BudgetTrend.STABLE
        }

        val changeRatio = (total - previousAverage) / previousAverage
        return when {
            changeRatio > TREND_THRESHOLD_RATIO -> BudgetTrend.INCREASING
            changeRatio < -TREND_THRESHOLD_RATIO -> BudgetTrend.DECREASING
            else -> BudgetTrend.STABLE
        }
    }

    companion object {
        /** Factor para proyectar el gasto semanal a un mes completo (365.25 dias / 12 meses / 7 dias). */
        const val MONTHLY_PROJECTION_FACTOR = 4.33

        /** Diferencia minima (5%) contra el promedio anterior para considerar que hay tendencia. */
        private const val TREND_THRESHOLD_RATIO = 0.05

        /** Incremento minimo (15%) sobre el promedio historico para disparar una [PriceAlert]. */
        const val PRICE_ALERT_THRESHOLD_RATIO = 0.15
    }
}
