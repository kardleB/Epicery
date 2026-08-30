package com.epicery.app.domain.model

import com.epicery.app.data.local.FoodGroup

/**
 * Alimento del catalogo tal como lo consume la capa de dominio, sin
 * anotaciones de persistencia (esas viven en `FoodItemEntity`).
 */
data class FoodItem(
    val id: Long = 0,
    val name: String,
    val foodGroup: FoodGroup,
    val category: String,
    val servingSizeGrams: Double = 100.0,
    val calories: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val fiberGrams: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val addedSugarGrams: Double = 0.0,
    val isWholeGrain: Boolean = false,
    val isProcessed: Boolean = false
)
