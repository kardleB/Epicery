package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodItems: List<FoodItemEntity>): List<Long>

    @Update
    suspend fun update(foodItem: FoodItemEntity)

    @Delete
    suspend fun delete(foodItem: FoodItemEntity)

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getById(id: Long): FoodItemEntity?

    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAll(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE foodGroup = :foodGroup ORDER BY name ASC")
    fun getByFoodGroup(foodGroup: FoodGroup): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<FoodItemEntity>>
}
