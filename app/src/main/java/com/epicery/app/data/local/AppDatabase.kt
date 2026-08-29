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
        ShoppingListItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(FoodGroupConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun shoppingListDao(): ShoppingListDao
}
