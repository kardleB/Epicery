package com.epicery.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Alimento base del catálogo, con su información nutricional y su
 * grupo alimenticio (RF1: los 5 grupos de Dietary Guidelines 2025-2030).
 */
@Entity(
    tableName = "food_items",
    indices = [Index(value = ["foodGroup"])]
)
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val foodGroup: FoodGroup,
    val category: String,
    @ColumnInfo(defaultValue = "1") val servingSizeGrams: Double = 100.0,
    @ColumnInfo(defaultValue = "0") val calories: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val proteinGrams: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val carbsGrams: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val fatGrams: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val fiberGrams: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val sodiumMg: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val addedSugarGrams: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val isWholeGrain: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isProcessed: Boolean = false
)
