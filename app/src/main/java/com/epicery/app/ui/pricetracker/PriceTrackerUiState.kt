package com.epicery.app.ui.pricetracker

import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.model.PriceAlert
import com.epicery.app.domain.model.PriceHistory

/** Tendencia de precio de un artículo, calculada comparando el último precio contra el
 * promedio de los registros anteriores (RF3). */
enum class PriceTrend {
    UP, DOWN, STABLE
}

/**
 * Estado de la pantalla Price Tracker (ver `docs/design/wireframes.md`, sección "3. Price
 * Tracker"). [priceHistory] viene ordenado cronológicamente (más viejo primero) para alimentar
 * [PriceChart] directamente. [priceAlert] se genera con
 * [com.epicery.app.domain.calculator.BudgetCalculator.alertIfOverAverage] y dispara la alerta de
 * precio alto (RF3, CA2) cuando el último precio registrado supera en más de un 15% el promedio
 * histórico del artículo.
 */
data class PriceTrackerUiState(
    val isLoading: Boolean = true,
    val foodItems: List<FoodItem> = emptyList(),
    val selectedFoodItem: FoodItem? = null,
    val priceHistory: List<PriceHistory> = emptyList(),
    val averagePrice: Double = 0.0,
    val latestPrice: Double? = null,
    val trend: PriceTrend = PriceTrend.STABLE,
    val priceAlert: PriceAlert? = null
) {
    val hasHistory: Boolean get() = priceHistory.size >= 2
    val isPriceHigh: Boolean get() = priceAlert != null
}
