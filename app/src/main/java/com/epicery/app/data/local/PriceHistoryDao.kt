package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(priceHistory: PriceHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(priceHistories: List<PriceHistoryEntity>): List<Long>

    @Update
    suspend fun update(priceHistory: PriceHistoryEntity)

    @Delete
    suspend fun delete(priceHistory: PriceHistoryEntity)

    @Query("SELECT * FROM price_history WHERE id = :id")
    suspend fun getById(id: Long): PriceHistoryEntity?

    @Query("SELECT * FROM price_history ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE foodItemId = :foodItemId ORDER BY recordedAt DESC")
    fun getHistoryForFoodItem(foodItemId: Long): Flow<List<PriceHistoryEntity>>

    @Query(
        "SELECT * FROM price_history WHERE foodItemId = :foodItemId " +
            "ORDER BY recordedAt DESC LIMIT 1"
    )
    suspend fun getLatestPriceForFoodItem(foodItemId: Long): PriceHistoryEntity?
}
