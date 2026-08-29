package com.epicery.app.data.local

import androidx.room.TypeConverter

/**
 * Los 5 grupos alimenticios definidos por Dietary Guidelines 2025-2030 (RF1).
 */
enum class FoodGroup {
    FRUITS,
    VEGETABLES,
    GRAINS,
    PROTEIN,
    DAIRY
}

class FoodGroupConverter {
    @TypeConverter
    fun fromFoodGroup(value: FoodGroup): String = value.name

    @TypeConverter
    fun toFoodGroup(value: String): FoodGroup = FoodGroup.valueOf(value)
}
