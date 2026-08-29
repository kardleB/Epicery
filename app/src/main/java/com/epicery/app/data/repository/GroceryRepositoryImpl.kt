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
        dao.getAll().map { entities ->
            entities.map { GroceryItem(it.id, it.name, it.foodGroup, it.estimatedPrice) }
        }

    override suspend fun addGroceryItem(item: GroceryItem) {
        dao.insert(GroceryItemEntity(item.id, item.name, item.foodGroup, item.estimatedPrice))
    }
}
