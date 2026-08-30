package com.epicery.app.domain.repository

import com.epicery.app.domain.model.UsdaNutritionInfo

/**
 * Acceso a la API pública de USDA FoodData Central (RF1, CA1): busca información
 * nutricional (calorías, proteína, sodio, azúcar) por nombre de alimento.
 */
interface UsdaFoodDataRepository {
    suspend fun searchNutrition(query: String): UsdaNutritionInfo?
}
