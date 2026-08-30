package com.epicery.app.ui.shoppinglist

import androidx.compose.ui.graphics.Color
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.ui.theme.FoodGroupDairy
import com.epicery.app.ui.theme.FoodGroupFruits
import com.epicery.app.ui.theme.FoodGroupGrains
import com.epicery.app.ui.theme.FoodGroupProtein
import com.epicery.app.ui.theme.FoodGroupVegetables
import java.util.Locale

/** Nombre y color de acento por [FoodGroup] (ver `docs/design/wireframes.md`), compartidos entre
 * los componentes de esta pantalla ([FoodItemCard], [CategoryFilter], [BudgetHeader]). */
internal fun accentColor(group: FoodGroup): Color = when (group) {
    FoodGroup.FRUITS -> FoodGroupFruits
    FoodGroup.VEGETABLES -> FoodGroupVegetables
    FoodGroup.GRAINS -> FoodGroupGrains
    FoodGroup.PROTEIN -> FoodGroupProtein
    FoodGroup.DAIRY -> FoodGroupDairy
}

internal fun displayName(group: FoodGroup): String = when (group) {
    FoodGroup.FRUITS -> "Frutas"
    FoodGroup.VEGETABLES -> "Vegetales"
    FoodGroup.GRAINS -> "Granos"
    FoodGroup.PROTEIN -> "Proteínas"
    FoodGroup.DAIRY -> "Lácteos"
}

internal fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"
