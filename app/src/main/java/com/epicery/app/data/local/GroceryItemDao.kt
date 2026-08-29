package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items")
    fun getAll(): Flow<List<GroceryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryItemEntity)
}
