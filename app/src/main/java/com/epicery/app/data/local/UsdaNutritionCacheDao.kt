package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsdaNutritionCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: UsdaNutritionCacheEntity)

    @Query("SELECT * FROM usda_nutrition_cache WHERE `query` = :query")
    suspend fun getByQuery(query: String): UsdaNutritionCacheEntity?
}
