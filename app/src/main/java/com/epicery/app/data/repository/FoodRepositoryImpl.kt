package com.epicery.app.data.repository

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.data.local.FoodItemDao
import com.epicery.app.data.local.FoodItemEntity
import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val dao: FoodItemDao
) : FoodRepository {

    override fun getFoodItems(): Flow<List<FoodItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>> =
        dao.getByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override fun getFoodItemsByFoodGroup(foodGroup: FoodGroup): Flow<List<FoodItem>> =
        dao.getByFoodGroup(foodGroup).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getFoodItemById(id: Long): FoodItem? =
        dao.getById(id)?.toDomain()

    override suspend fun saveFoodItem(foodItem: FoodItem): Long =
        dao.insert(foodItem.toEntity())

    override suspend fun saveFoodItems(foodItems: List<FoodItem>): List<Long> =
        dao.insertAll(foodItems.map { it.toEntity() })
}

private fun FoodItemEntity.toDomain() = FoodItem(
    id = id,
    name = name,
    foodGroup = foodGroup,
    category = category,
    servingSizeGrams = servingSizeGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sodiumMg = sodiumMg,
    addedSugarGrams = addedSugarGrams,
    isWholeGrain = isWholeGrain,
    isProcessed = isProcessed
)

private fun FoodItem.toEntity() = FoodItemEntity(
    id = id,
    name = name,
    foodGroup = foodGroup,
    category = category,
    servingSizeGrams = servingSizeGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sodiumMg = sodiumMg,
    addedSugarGrams = addedSugarGrams,
    isWholeGrain = isWholeGrain,
    isProcessed = isProcessed
)
