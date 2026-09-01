package com.epicery.app.domain.model

/**
 * Lista de compras con sus items y precios estimados, tal como la consume
 * [com.epicery.app.domain.calculator.BudgetCalculator] para estimar el presupuesto semanal (RF4).
 */
data class ShoppingList(
    val id: Long = 0,
    val name: String = "",
    val items: List<GroceryItem> = emptyList()
)
