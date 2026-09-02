package com.epicery.app.ui.shoppinglist

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem

/**
 * Estado de la pantalla Shopping List (ver `docs/design/wireframes.md`, sección "2. Shopping
 * List"). [itemsByGroup] ya viene agrupado por [FoodGroup] y filtrado por [selectedFoodGroup]
 * (`null` = "Todos"); [totalEstimated] es la suma de todos los items de la lista, sin importar el
 * filtro activo, para que el footer de presupuesto siempre refleje el total real de la lista.
 * [allItems] mantiene la lista completa sin el filtro de categoría, para que exportar/compartir
 * (RF2) siempre incluya la lista entera y no solo lo que está visible en pantalla.
 */
data class ShoppingListUiState(
    val isLoading: Boolean = true,
    val weeklyBudget: Double = 0.0,
    val totalEstimated: Double = 0.0,
    val selectedFoodGroup: FoodGroup? = null,
    val itemsByGroup: Map<FoodGroup, List<GroceryItem>> = emptyMap(),
    val allItems: List<GroceryItem> = emptyList()
) {
    val isEmpty: Boolean get() = itemsByGroup.values.all { it.isEmpty() }
}
