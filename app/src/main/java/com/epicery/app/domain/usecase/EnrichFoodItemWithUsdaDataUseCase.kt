package com.epicery.app.domain.usecase

import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.repository.FoodRepository
import com.epicery.app.domain.repository.UsdaFoodDataRepository
import javax.inject.Inject

/**
 * Busca un alimento por nombre en USDA FoodData Central y actualiza sus valores
 * nutricionales (calorías, proteína, sodio, azúcar) en el catálogo local (RF1, CA1).
 * Si USDA no tiene una coincidencia, devuelve null y no modifica el catálogo: es el
 * comportamiento esperado de una búsqueda (no toda consulta tiene resultado), no un error.
 *
 * `foodItem` siempre proviene del catálogo local (ya tiene un `id` asignado por Room), así
 * que no hace falta "verificar" su existencia antes de guardar: `foodRepository.saveFoodItem`
 * hace un upsert por `id` (`FoodItemDao.insert` usa `OnConflictStrategy.REPLACE` sobre la
 * primary key), y `foodItem.copy(...)` preserva ese `id`. El resultado es siempre una
 * actualización de la fila existente, nunca un duplicado.
 */
class EnrichFoodItemWithUsdaDataUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
    private val usdaFoodDataRepository: UsdaFoodDataRepository
) {
    suspend operator fun invoke(foodItem: FoodItem): FoodItem? {
        val nutrition = usdaFoodDataRepository.searchNutrition(foodItem.name) ?: return null
        val enriched = foodItem.copy(
            calories = nutrition.calories,
            proteinGrams = nutrition.proteinGrams,
            sodiumMg = nutrition.sodiumMg,
            addedSugarGrams = nutrition.sugarGrams
        )
        foodRepository.saveFoodItem(enriched)
        return enriched
    }
}
