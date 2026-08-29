package com.epicery.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GroceryItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryItemDao(): GroceryItemDao
}
