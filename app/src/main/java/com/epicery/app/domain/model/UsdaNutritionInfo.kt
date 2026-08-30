package com.epicery.app.domain.model

/**
 * Información nutricional obtenida desde USDA FoodData Central para
 * poblar o enriquecer un [FoodItem] del catálogo (RF1, CA1).
 */
data class UsdaNutritionInfo(
    val fdcId: Long,
    val description: String,
    val calories: Double,
    val proteinGrams: Double,
    val sodiumMg: Double,
    val sugarGrams: Double
)
