package com.epicery.app.ui.shoppinglist

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem

/**
 * Arma el texto plano que se comparte por WhatsApp/email al exportar la lista (RF2). Recibe las
 * etiquetas ya resueltas (en vez de llamar a [com.epicery.app.ui.common.foodGroupLabel] directamente)
 * para que la función se pueda testear sin depender de Compose ni de un [android.content.Context].
 */
fun formatShoppingListForSharing(
    items: List<GroceryItem>,
    groupLabels: Map<FoodGroup, String>,
    totalLabel: String
): String {
    val sections = FoodGroup.entries
        .map { group -> group to items.filter { it.foodGroup.equals(group.name, ignoreCase = true) } }
        .filter { (_, groupItems) -> groupItems.isNotEmpty() }
        .joinToString(separator = "\n\n") { (group, groupItems) ->
            val lines = groupItems.joinToString(separator = "\n") { item ->
                val checkbox = if (item.isPurchased) "[x]" else "[ ]"
                "$checkbox ${item.name} - ${formatPrice(item.estimatedPrice)}"
            }
            "${groupLabels.getValue(group)}:\n$lines"
        }
    val total = items.sumOf { it.estimatedPrice }
    return "$sections\n\n$totalLabel: ${formatPrice(total)}"
}
