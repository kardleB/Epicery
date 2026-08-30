package com.epicery.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Caché persistida de respuestas de USDA FoodData Central, indexada por el término de
 * búsqueda normalizado (RNF5: soporte offline-first, evita repetir llamadas de red ya
 * resueltas recientemente).
 */
@Entity(tableName = "usda_nutrition_cache")
data class UsdaNutritionCacheEntity(
    @PrimaryKey val query: String,
    val fdcId: Long,
    val description: String,
    val calories: Double,
    val proteinGrams: Double,
    val sodiumMg: Double,
    val sugarGrams: Double,
    val fetchedAt: Long
)
