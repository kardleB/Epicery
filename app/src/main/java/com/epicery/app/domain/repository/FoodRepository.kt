package com.epicery.app.domain.repository

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.FoodItem
import kotlinx.coroutines.flow.Flow

/**
 * Acceso al catalogo de alimentos (RF1). Encapsula el `FoodItemDao` de Room
 * para que los casos de uso del dominio no dependan de detalles de Room.
 */
interface FoodRepository {
    fun getFoodItems(): Flow<List<FoodItem>>
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>>
    fun getFoodItemsByFoodGroup(foodGroup: FoodGroup): Flow<List<FoodItem>>
    suspend fun getFoodItemById(id: Long): FoodItem?
    suspend fun saveFoodItem(foodItem: FoodItem): Long
    suspend fun saveFoodItems(foodItems: List<FoodItem>): List<Long>
}
