package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface GroceryPriceCacheDao {
    @Query("SELECT * FROM grocery_price_cache WHERE `query` = :query")
    suspend fun getByQuery(query: String): List<GroceryPriceCacheEntity>

    @Insert
    suspend fun insertAll(entries: List<GroceryPriceCacheEntity>)

    @Query("DELETE FROM grocery_price_cache WHERE `query` = :query")
    suspend fun deleteByQuery(query: String)

    @Transaction
    suspend fun replaceForQuery(query: String, entries: List<GroceryPriceCacheEntity>) {
        deleteByQuery(query)
        if (entries.isNotEmpty()) {
            insertAll(entries)
        }
    }
}
