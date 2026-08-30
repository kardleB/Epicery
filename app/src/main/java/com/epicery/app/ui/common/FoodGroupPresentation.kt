package com.epicery.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.ui.theme.FoodGroupDairy
import com.epicery.app.ui.theme.FoodGroupFruits
import com.epicery.app.ui.theme.FoodGroupGrains
import com.epicery.app.ui.theme.FoodGroupProtein
import com.epicery.app.ui.theme.FoodGroupVegetables

/**
 * Nombre y color de acento por [FoodGroup] (ver `docs/design/wireframes.md`), compartidos entre
 * Home, Budget y Shopping List (RF6: unica fuente de las traducciones para evitar que queden
 * desincronizadas entre pantallas).
 */
@Composable
fun foodGroupLabel(group: FoodGroup): String = stringResource(
    when (group) {
        FoodGroup.FRUITS -> R.string.food_group_fruits
        FoodGroup.VEGETABLES -> R.string.food_group_vegetables
        FoodGroup.GRAINS -> R.string.food_group_grains
        FoodGroup.PROTEIN -> R.string.food_group_protein
        FoodGroup.DAIRY -> R.string.food_group_dairy
    }
)

fun foodGroupAccentColor(group: FoodGroup): Color = when (group) {
    FoodGroup.FRUITS -> FoodGroupFruits
    FoodGroup.VEGETABLES -> FoodGroupVegetables
    FoodGroup.GRAINS -> FoodGroupGrains
    FoodGroup.PROTEIN -> FoodGroupProtein
    FoodGroup.DAIRY -> FoodGroupDairy
}
