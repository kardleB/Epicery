package com.epicery.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GroceryItemEntity::class,
        FoodItemEntity::class,
        PriceHistoryEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        UsdaNutritionCacheEntity::class,
        GroceryPriceCacheEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(FoodGroupConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun usdaNutritionCacheDao(): UsdaNutritionCacheDao
    abstract fun groceryPriceCacheDao(): GroceryPriceCacheDao
}
