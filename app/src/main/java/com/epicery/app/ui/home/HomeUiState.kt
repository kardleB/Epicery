package com.epicery.app.ui.home

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem

/**
 * Estado de la pantalla Home (ver `docs/design/wireframes.md`, sección "1. Home").
 *
 * [isLoading] arranca en `true` y pasa a `false` en cuanto llega la primera emisión de la lista
 * de compras desde Room (cache local, sin esperar red) para poder pintar la pantalla en menos de
 * 2 segundos (RNF5); el presupuesto semanal se actualiza después de forma independiente.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val hasActiveList: Boolean = false,
    val weeklyBudget: Double = 0.0,
    val amountSpent: Double = 0.0,
    val shoppingListPreview: List<GroceryItem> = emptyList(),
    val foodGroupCounts: Map<FoodGroup, Int> = emptyMap()
)
