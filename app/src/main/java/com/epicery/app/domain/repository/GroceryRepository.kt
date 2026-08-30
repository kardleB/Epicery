package com.epicery.app.domain.repository

import com.epicery.app.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

interface GroceryRepository {
    fun getGroceryItems(): Flow<List<GroceryItem>>
    suspend fun addGroceryItem(item: GroceryItem)
    suspend fun updateGroceryItem(item: GroceryItem)
}
