package com.epicery.app.data.repository

import com.epicery.app.data.local.GroceryItemDao
import com.epicery.app.data.local.GroceryItemEntity
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.repository.GroceryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GroceryRepositoryImpl @Inject constructor(
    private val dao: GroceryItemDao
) : GroceryRepository {

    override fun getGroceryItems(): Flow<List<GroceryItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addGroceryItem(item: GroceryItem) {
        dao.insert(item.toEntity())
    }

    override suspend fun updateGroceryItem(item: GroceryItem) {
        dao.update(item.toEntity())
    }

    private fun GroceryItemEntity.toDomain() = GroceryItem(
        id = id,
        name = name,
        foodGroup = foodGroup,
        estimatedPrice = estimatedPrice,
        isPurchased = isPurchased,
        purchasedAt = purchasedAt,
        pricePaid = pricePaid
    )

    private fun GroceryItem.toEntity() = GroceryItemEntity(
        id = id,
        name = name,
        foodGroup = foodGroup,
        estimatedPrice = estimatedPrice,
        isPurchased = isPurchased,
        purchasedAt = purchasedAt,
        pricePaid = pricePaid
    )
}
