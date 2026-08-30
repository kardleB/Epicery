package com.epicery.app.data.repository

import com.epicery.app.BuildConfig
import com.epicery.app.data.remote.UsdaFood
import com.epicery.app.data.remote.UsdaFoodDataApi
import com.epicery.app.domain.model.UsdaNutritionInfo
import com.epicery.app.domain.repository.UsdaFoodDataRepository
import javax.inject.Inject

class UsdaFoodDataRepositoryImpl @Inject constructor(
    private val api: UsdaFoodDataApi
) : UsdaFoodDataRepository {

    override suspend fun searchNutrition(query: String): UsdaNutritionInfo? {
        check(BuildConfig.USDA_API_KEY.isNotBlank()) {
            "Falta configurar USDA_API_KEY en local.properties (ver README)."
        }
        val response = api.searchFoods(query = query, apiKey = BuildConfig.USDA_API_KEY)
        return response.foods.orEmpty().firstOrNull()?.toNutritionInfo()
    }
}

private fun UsdaFood.toNutritionInfo() = UsdaNutritionInfo(
    fdcId = fdcId,
    description = description.orEmpty(),
    calories = nutrientValue("Energy"),
    proteinGrams = nutrientValue("Protein"),
    sodiumMg = nutrientValue("Sodium"),
    sugarGrams = nutrientValue("Sugars, added").takeIf { it > 0.0 }
        ?: nutrientValue("Total Sugars", "Sugars, total")
)

private fun UsdaFood.nutrientValue(vararg nameContains: String): Double =
    foodNutrients.orEmpty().firstOrNull { nutrient ->
        nameContains.any { nutrient.nutrientName.orEmpty().contains(it, ignoreCase = true) }
    }?.value ?: 0.0
