package com.epicery.app.ui.shoppinglist

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica que [formatShoppingListForSharing] arme un texto plano apto para compartir por
 * WhatsApp/email (RF2): agrupado por grupo alimenticio, con precios y el total de la lista.
 */
class ShoppingListShareFormatterTest {

    private val groupLabels = mapOf(
        FoodGroup.FRUITS to "Frutas",
        FoodGroup.VEGETABLES to "Vegetales",
        FoodGroup.GRAINS to "Granos",
        FoodGroup.PROTEIN to "Proteinas",
        FoodGroup.DAIRY to "Lacteos"
    )

    private fun groceryItem(name: String, foodGroup: String, price: Double, isPurchased: Boolean = false) =
        GroceryItem(name = name, foodGroup = foodGroup, estimatedPrice = price, isPurchased = isPurchased)

    @Test
    fun `groups items by food group and lists their prices`() {
        val items = listOf(
            groceryItem("Manzana", "FRUITS", 3.50),
            groceryItem("Leche", "DAIRY", 4.25)
        )

        val shareText = formatShoppingListForSharing(items, groupLabels, totalLabel = "Total")

        assertTrue(shareText.contains("Frutas:\n[ ] Manzana - $3.50"))
        assertTrue(shareText.contains("Lacteos:\n[ ] Leche - $4.25"))
    }

    @Test
    fun `marks purchased items with a checked checkbox`() {
        val items = listOf(groceryItem("Pollo", "PROTEIN", 12.00, isPurchased = true))

        val shareText = formatShoppingListForSharing(items, groupLabels, totalLabel = "Total")

        assertTrue(shareText.contains("[x] Pollo - $12.00"))
    }

    @Test
    fun `appends the total of all items regardless of group`() {
        val items = listOf(
            groceryItem("Manzana", "FRUITS", 3.50),
            groceryItem("Leche", "DAIRY", 4.25),
            groceryItem("Pollo", "PROTEIN", 12.00)
        )

        val shareText = formatShoppingListForSharing(items, groupLabels, totalLabel = "Total")

        assertTrue(shareText.trim().endsWith("Total: $19.75"))
    }

    @Test
    fun `omits food groups without items`() {
        val items = listOf(groceryItem("Manzana", "FRUITS", 3.50))

        val shareText = formatShoppingListForSharing(items, groupLabels, totalLabel = "Total")

        assertEquals(false, shareText.contains("Lacteos"))
        assertEquals(false, shareText.contains("Proteinas"))
    }
}
