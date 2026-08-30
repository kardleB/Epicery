package com.epicery.app.ui.pricetracker

import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.model.PriceHistory

/** Tendencia de precio de un artículo, calculada comparando el último precio contra el
 * promedio de los registros anteriores (RF3). */
enum class PriceTrend {
    UP, DOWN, STABLE
}

/**
 * Estado de la pantalla Price Tracker (ver `docs/design/wireframes.md`, sección "3. Price
 * Tracker"). [priceHistory] viene ordenado cronológicamente (más viejo primero) para alimentar
 * [PriceChart] directamente. [isPriceHigh] dispara la alerta de precio alto (CA2) cuando el
 * último precio registrado supera el promedio histórico del artículo.
 */
data class PriceTrackerUiState(
    val isLoading: Boolean = true,
    val foodItems: List<FoodItem> = emptyList(),
    val selectedFoodItem: FoodItem? = null,
    val priceHistory: List<PriceHistory> = emptyList(),
    val averagePrice: Double = 0.0,
    val latestPrice: Double? = null,
    val trend: PriceTrend = PriceTrend.STABLE
) {
    val hasHistory: Boolean get() = priceHistory.size >= 2
    val isPriceHigh: Boolean get() = latestPrice != null && averagePrice > 0.0 && latestPrice > averagePrice
}
