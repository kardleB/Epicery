package com.epicery.app.ui.shoppinglist

import java.util.Locale

/** Formato de precio compartido entre los componentes de esta pantalla ([FoodItemCard],
 * [CategoryFilter], [BudgetHeader]). Los nombres/colores por [com.epicery.app.data.local.FoodGroup]
 * viven en [com.epicery.app.ui.common.foodGroupLabel] / [com.epicery.app.ui.common.foodGroupAccentColor]
 * (RF6: unica fuente de las traducciones, compartida tambien con Home y Budget). */
internal fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"
