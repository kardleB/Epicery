package com.epicery.app.domain.model

/**
 * Alerta generada por [com.epicery.app.domain.calculator.BudgetCalculator.alertIfOverAverage]
 * cuando el precio actual de un articulo supera en mas de un 15% su promedio historico (RF3, CA2).
 */
data class PriceAlert(
    val itemName: String,
    val currentPrice: Double,
    val averagePrice: Double,
    val increaseRatio: Double
)
