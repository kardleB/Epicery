package com.epicery.app.domain.calculator

import com.epicery.app.domain.model.BudgetTrend
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.model.ShoppingList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifica que [BudgetCalculator.estimateWeeklyBudget] calcule correctamente el total, el
 * desglose por categoria, la tendencia contra semanas anteriores y la proyeccion mensual (RF4, CA3).
 */
class BudgetCalculatorTest {

    private val calculator = BudgetCalculator()

    private fun groceryItem(name: String, foodGroup: String, price: Double) =
        GroceryItem(name = name, foodGroup = foodGroup, estimatedPrice = price)

    @Test
    fun `sums estimated prices of all items into the total`() {
        val shoppingList = ShoppingList(
            items = listOf(
                groceryItem("Manzana", "FRUITS", 3.50),
                groceryItem("Leche", "DAIRY", 4.25),
                groceryItem("Pollo", "PROTEIN", 12.00)
            )
        )

        val estimate = calculator.estimateWeeklyBudget(shoppingList)

        assertEquals(19.75, estimate.total, 0.0001)
    }

    @Test
    fun `groups spending by food category`() {
        val shoppingList = ShoppingList(
            items = listOf(
                groceryItem("Manzana", "FRUITS", 3.50),
                groceryItem("Banana", "FRUITS", 1.50),
                groceryItem("Leche", "DAIRY", 4.25)
            )
        )

        val estimate = calculator.estimateWeeklyBudget(shoppingList)

        assertEquals(5.00, estimate.byCategory.getValue("FRUITS"), 0.0001)
        assertEquals(4.25, estimate.byCategory.getValue("DAIRY"), 0.0001)
    }

    @Test
    fun `projects monthly spend as total times 4_33`() {
        val shoppingList = ShoppingList(items = listOf(groceryItem("Arroz", "GRAINS", 10.0)))

        val estimate = calculator.estimateWeeklyBudget(shoppingList)

        assertEquals(43.30, estimate.monthlyProjection, 0.0001)
    }

    @Test
    fun `flags an increasing trend when total is clearly above previous weeks' average`() {
        val shoppingList = ShoppingList(items = listOf(groceryItem("Carne", "PROTEIN", 100.0)))

        val estimate = calculator.estimateWeeklyBudget(shoppingList, previousWeeklyTotals = listOf(50.0, 60.0, 55.0))

        assertEquals(BudgetTrend.INCREASING, estimate.trend)
    }

    @Test
    fun `flags a decreasing trend when total is clearly below previous weeks' average`() {
        val shoppingList = ShoppingList(items = listOf(groceryItem("Carne", "PROTEIN", 20.0)))

        val estimate = calculator.estimateWeeklyBudget(shoppingList, previousWeeklyTotals = listOf(50.0, 60.0, 55.0))

        assertEquals(BudgetTrend.DECREASING, estimate.trend)
    }

    @Test
    fun `flags a stable trend when total is close to previous weeks' average`() {
        val shoppingList = ShoppingList(items = listOf(groceryItem("Carne", "PROTEIN", 55.0)))

        val estimate = calculator.estimateWeeklyBudget(shoppingList, previousWeeklyTotals = listOf(50.0, 60.0, 55.0))

        assertEquals(BudgetTrend.STABLE, estimate.trend)
    }

    @Test
    fun `flags a stable trend when there are no previous weeks to compare against`() {
        val shoppingList = ShoppingList(items = listOf(groceryItem("Carne", "PROTEIN", 55.0)))

        val estimate = calculator.estimateWeeklyBudget(shoppingList, previousWeeklyTotals = emptyList())

        assertEquals(BudgetTrend.STABLE, estimate.trend)
    }

    @Test
    fun `returns a zeroed estimate for an empty shopping list`() {
        val estimate = calculator.estimateWeeklyBudget(ShoppingList(items = emptyList()))

        assertEquals(0.0, estimate.total, 0.0001)
        assertEquals(emptyMap<String, Double>(), estimate.byCategory)
        assertEquals(0.0, estimate.monthlyProjection, 0.0001)
        assertEquals(BudgetTrend.STABLE, estimate.trend)
    }

    @Test
    fun `generates a price alert when current price is more than 15 percent above the historical average`() {
        val alert = calculator.alertIfOverAverage(
            itemName = "Manzana",
            currentPrice = 4.60,
            historicalPrices = listOf(3.50, 4.00, 4.00)
        )

        requireNotNull(alert)
        assertEquals("Manzana", alert.itemName)
        assertEquals(4.60, alert.currentPrice, 0.0001)
        assertEquals(3.8333, alert.averagePrice, 0.0001)
        assertEquals(0.2, alert.increaseRatio, 0.001)
    }

    @Test
    fun `does not generate a price alert when the increase is 15 percent or less`() {
        val alert = calculator.alertIfOverAverage(
            itemName = "Manzana",
            currentPrice = 4.60,
            historicalPrices = listOf(4.00, 4.00, 4.00)
        )

        assertNull(alert)
    }

    @Test
    fun `does not generate a price alert when there is no price history`() {
        val alert = calculator.alertIfOverAverage(
            itemName = "Manzana",
            currentPrice = 4.60,
            historicalPrices = emptyList()
        )

        assertNull(alert)
    }

    @Test
    fun `does not generate a price alert when the historical average is zero`() {
        val alert = calculator.alertIfOverAverage(
            itemName = "Manzana",
            currentPrice = 4.60,
            historicalPrices = listOf(0.0, 0.0)
        )

        assertNull(alert)
    }
}
