package com.epicery.app.domain.usecase

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Da de alta un [FoodItem] en el catálogo (RF1) a partir de un nombre y grupo alimenticio si
 * todavía no existe uno con ese nombre (comparación case-insensitive, ver `FoodItemDao.getByName`),
 * para que Price Tracker tenga algo sobre lo cual seleccionar y comparar precios. Si ya existe,
 * no lo modifica: `GroceryItem` (lista de compras) y `FoodItem` (catálogo nutricional) son
 * entidades separadas con su propio ciclo de vida, esto solo asegura que la segunda exista.
 */
class SyncFoodItemToCatalogUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(name: String, foodGroup: FoodGroup) {
        if (foodRepository.getFoodItemByName(name) != null) return
        foodRepository.saveFoodItem(FoodItem(name = name, foodGroup = foodGroup, category = foodGroup.name))
    }
}
