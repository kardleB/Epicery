package com.epicery.app.data.local

import androidx.room.TypeConverter

/**
 * Categorías de la lista de compras: los 5 grupos alimenticios base definidos por Dietary
 * Guidelines 2025-2030 (RF1), más [OILS] (aceites, separado de [GRAINS]), [CEREALS] (cereales de
 * desayuno de caja -- Corn Flakes, avena, granola -- distinto de [GRAINS], que sigue cubriendo
 * arroz/pasta/harina) y [CLEANING] (productos de limpieza del hogar, la única categoría no
 * alimenticia de la lista).
 */
enum class FoodGroup {
    FRUITS,
    VEGETABLES,
    GRAINS,
    PROTEIN,
    DAIRY,
    OILS,
    CEREALS,
    CLEANING
}

class FoodGroupConverter {
    @TypeConverter
    fun fromFoodGroup(value: FoodGroup): String = value.name

    @TypeConverter
    fun toFoodGroup(value: String): FoodGroup = FoodGroup.valueOf(value)
}
