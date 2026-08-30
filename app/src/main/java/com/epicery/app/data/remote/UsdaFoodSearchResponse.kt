package com.epicery.app.data.remote

/**
 * Respuesta de `GET /foods/search` de USDA FoodData Central.
 *
 * Los campos son nullable porque Gson puede instanciar estas data classes sin pasar por el
 * constructor (bypasseando los valores por defecto de Kotlin) cuando una clave falta en el JSON.
 */
data class UsdaFoodSearchResponse(
    val totalHits: Int = 0,
    val foods: List<UsdaFood>? = null
)

data class UsdaFood(
    val fdcId: Long = 0,
    val description: String? = null,
    val foodNutrients: List<UsdaFoodNutrient>? = null
)

data class UsdaFoodNutrient(
    val nutrientName: String? = null,
    val nutrientNumber: String? = null,
    val unitName: String? = null,
    val value: Double = 0.0
)
