package com.epicery.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val foodGroup: String,
    val estimatedPrice: Double,
    @ColumnInfo(defaultValue = "0") val isPurchased: Boolean = false
)
