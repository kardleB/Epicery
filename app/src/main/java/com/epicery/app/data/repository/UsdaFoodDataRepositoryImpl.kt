package com.epicery.app.data.repository

import com.epicery.app.BuildConfig
import com.epicery.app.data.local.UsdaNutritionCacheDao
import com.epicery.app.data.local.UsdaNutritionCacheEntity
import com.epicery.app.data.remote.UsdaFood
import com.epicery.app.data.remote.UsdaFoodDataApi
import com.epicery.app.domain.model.UsdaNutritionInfo
import com.epicery.app.domain.repository.UsdaFoodDataRepository
import com.epicery.app.util.Constants
import java.io.IOException
import javax.inject.Inject

/**
 * Cachea las respuestas de USDA FoodData Central en Room, indexadas por término de
 * búsqueda (RNF5): si la cache está vigente evita la llamada de red, y si la llamada
 * falla por falta de conexión devuelve la última respuesta cacheada en vez de fallar.
 */
class UsdaFoodDataRepositoryImpl @Inject constructor(
    private val api: UsdaFoodDataApi,
    private val cacheDao: UsdaNutritionCacheDao
) : UsdaFoodDataRepository {

    override suspend fun searchNutrition(query: String): UsdaNutritionInfo? {
        val normalizedQuery = query.trim().lowercase()
        val cached = cacheDao.getByQuery(normalizedQuery)
        val cacheIsFresh = cached != null &&
            System.currentTimeMillis() - cached.fetchedAt < Constants.API_CACHE_TTL_MS
        if (cacheIsFresh) {
            return cached!!.toNutritionInfo()
        }

        return try {
            check(BuildConfig.USDA_API_KEY.isNotBlank()) {
                "Falta configurar USDA_API_KEY en local.properties (ver README)."
            }
            val response = api.searchFoods(query = query, apiKey = BuildConfig.USDA_API_KEY)
            val nutrition = response.foods.orEmpty().firstOrNull()?.toNutritionInfo()
            if (nutrition != null) {
                cacheDao.upsert(nutrition.toCacheEntity(normalizedQuery))
            }
            nutrition
        } catch (e: IOException) {
            cached?.toNutritionInfo() ?: throw e
        }
    }
}

private fun UsdaNutritionCacheEntity.toNutritionInfo() = UsdaNutritionInfo(
    fdcId = fdcId,
    description = description,
    calories = calories,
    proteinGrams = proteinGrams,
    sodiumMg = sodiumMg,
    sugarGrams = sugarGrams
)

private fun UsdaNutritionInfo.toCacheEntity(normalizedQuery: String) = UsdaNutritionCacheEntity(
    query = normalizedQuery,
    fdcId = fdcId,
    description = description,
    calories = calories,
    proteinGrams = proteinGrams,
    sodiumMg = sodiumMg,
    sugarGrams = sugarGrams,
    fetchedAt = System.currentTimeMillis()
)

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
