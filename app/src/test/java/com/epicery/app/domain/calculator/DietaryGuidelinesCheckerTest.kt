package com.epicery.app.domain.calculator

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.DietaryFlagReason
import com.epicery.app.domain.model.FoodItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica que [DietaryGuidelinesChecker.evaluate] detecte correctamente las porciones faltantes
 * por grupo alimenticio y marque los items procesados o con exceso de sodio/azucar (RF1, CA1).
 */
class DietaryGuidelinesCheckerTest {

    private val checker = DietaryGuidelinesChecker()

    private fun foodItem(
        name: String,
        foodGroup: FoodGroup,
        sodiumMg: Double = 0.0,
        addedSugarGrams: Double = 0.0,
        isProcessed: Boolean = false
    ) = FoodItem(
        name = name,
        foodGroup = foodGroup,
        category = foodGroup.name,
        sodiumMg = sodiumMg,
        addedSugarGrams = addedSugarGrams,
        isProcessed = isProcessed
    )

    @Test
    fun `reports every food group as missing for an empty shopping list`() {
        val report = checker.evaluate(emptyList())

        assertEquals(DietaryGuidelinesChecker.RECOMMENDED_WEEKLY_SERVINGS.size, report.missingFoodGroups.size)
        assertTrue(report.flaggedItems.isEmpty())
        assertFalse(report.isCompliant)
    }

    @Test
    fun `does not report a food group as missing once it reaches its recommended weekly servings`() {
        val recommended = DietaryGuidelinesChecker.RECOMMENDED_WEEKLY_SERVINGS.getValue(FoodGroup.FRUITS)
        val items = (1..recommended).map { foodItem("Fruta $it", FoodGroup.FRUITS) }

        val report = checker.evaluate(items)

        assertTrue(report.missingFoodGroups.none { it.foodGroup == FoodGroup.FRUITS })
    }

    @Test
    fun `reports the shortfall of servings for a food group below its recommendation`() {
        val items = listOf(foodItem("Manzana", FoodGroup.FRUITS))

        val report = checker.evaluate(items)

        val missingFruits = report.missingFoodGroups.first { it.foodGroup == FoodGroup.FRUITS }
        assertEquals(1, missingFruits.presentServings)
        assertEquals(DietaryGuidelinesChecker.RECOMMENDED_WEEKLY_SERVINGS.getValue(FoodGroup.FRUITS), missingFruits.recommendedServings)
        assertEquals(missingFruits.recommendedServings - 1, missingFruits.missingServings)
    }

    @Test
    fun `flags a highly processed item`() {
        val items = listOf(foodItem("Papas fritas", FoodGroup.GRAINS, isProcessed = true))

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals("Papas fritas", flagged.itemName)
        assertEquals(listOf(DietaryFlagReason.HIGHLY_PROCESSED), flagged.reasons)
    }

    @Test
    fun `flags an item at or above 20 percent of the daily sodium limit`() {
        val items = listOf(foodItem("Sopa enlatada", FoodGroup.VEGETABLES, sodiumMg = 460.0))

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals(listOf(DietaryFlagReason.EXCESS_SODIUM), flagged.reasons)
    }

    @Test
    fun `does not flag an item below the high sodium threshold`() {
        val items = listOf(foodItem("Pan", FoodGroup.GRAINS, sodiumMg = 200.0))

        val report = checker.evaluate(items)

        assertTrue(report.flaggedItems.isEmpty())
    }

    @Test
    fun `flags an item with 10 grams or more of added sugar`() {
        val items = listOf(foodItem("Yogur azucarado", FoodGroup.DAIRY, addedSugarGrams = 12.0))

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals(listOf(DietaryFlagReason.EXCESS_ADDED_SUGAR), flagged.reasons)
    }

    @Test
    fun `flags an item with multiple reasons at once`() {
        val items = listOf(
            foodItem("Cereal azucarado", FoodGroup.GRAINS, sodiumMg = 500.0, addedSugarGrams = 15.0, isProcessed = true)
        )

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals(
            setOf(DietaryFlagReason.HIGHLY_PROCESSED, DietaryFlagReason.EXCESS_SODIUM, DietaryFlagReason.EXCESS_ADDED_SUGAR),
            flagged.reasons.toSet()
        )
    }

    @Test
    fun `flags an item that is highly processed and has excess sodium but not excess sugar`() {
        val items = listOf(
            foodItem("Sopa instantanea", FoodGroup.VEGETABLES, sodiumMg = 500.0, isProcessed = true)
        )

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals(
            setOf(DietaryFlagReason.HIGHLY_PROCESSED, DietaryFlagReason.EXCESS_SODIUM),
            flagged.reasons.toSet()
        )
    }

    @Test
    fun `flags an item that is highly processed and has excess added sugar but not excess sodium`() {
        val items = listOf(
            foodItem("Galletas dulces", FoodGroup.GRAINS, addedSugarGrams = 15.0, isProcessed = true)
        )

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals(
            setOf(DietaryFlagReason.HIGHLY_PROCESSED, DietaryFlagReason.EXCESS_ADDED_SUGAR),
            flagged.reasons.toSet()
        )
    }

    @Test
    fun `flags an item with excess sodium and excess added sugar but not highly processed`() {
        val items = listOf(
            foodItem("Salsa dulce casera", FoodGroup.VEGETABLES, sodiumMg = 500.0, addedSugarGrams = 15.0)
        )

        val report = checker.evaluate(items)

        val flagged = report.flaggedItems.single()
        assertEquals(
            setOf(DietaryFlagReason.EXCESS_SODIUM, DietaryFlagReason.EXCESS_ADDED_SUGAR),
            flagged.reasons.toSet()
        )
    }

    @Test
    fun `sums the sodium of all items and flags when it exceeds the daily limit`() {
        val items = listOf(
            foodItem("Item 1", FoodGroup.PROTEIN, sodiumMg = 1200.0),
            foodItem("Item 2", FoodGroup.PROTEIN, sodiumMg = 1200.0)
        )

        val report = checker.evaluate(items)

        assertEquals(2400.0, report.totalSodiumMg, 0.0001)
        assertTrue(report.exceedsDailySodiumLimit)
    }

    @Test
    fun `does not flag the sodium total when it is within the daily limit`() {
        val items = listOf(foodItem("Item 1", FoodGroup.PROTEIN, sodiumMg = 100.0))

        val report = checker.evaluate(items)

        assertFalse(report.exceedsDailySodiumLimit)
    }

    @Test
    fun `does not suggest items for a missing food group when no catalog is provided`() {
        val items = listOf(foodItem("Manzana", FoodGroup.FRUITS))

        val report = checker.evaluate(items)

        val missingFruits = report.missingFoodGroups.first { it.foodGroup == FoodGroup.FRUITS }
        assertTrue(missingFruits.suggestedItems.isEmpty())
    }

    @Test
    fun `suggests catalog items belonging to a missing food group`() {
        val items = listOf(foodItem("Manzana", FoodGroup.FRUITS))
        val catalog = listOf(
            foodItem("Banana", FoodGroup.FRUITS),
            foodItem("Pollo", FoodGroup.PROTEIN)
        )

        val report = checker.evaluate(items, catalog)

        val missingFruits = report.missingFoodGroups.first { it.foodGroup == FoodGroup.FRUITS }
        assertEquals(listOf("Banana"), missingFruits.suggestedItems.map { it.name })
    }

    @Test
    fun `does not suggest an item that is already in the shopping list`() {
        val items = listOf(foodItem("Manzana", FoodGroup.FRUITS))
        val catalog = listOf(foodItem("Manzana", FoodGroup.FRUITS), foodItem("Banana", FoodGroup.FRUITS))

        val report = checker.evaluate(items, catalog)

        val missingFruits = report.missingFoodGroups.first { it.foodGroup == FoodGroup.FRUITS }
        assertEquals(listOf("Banana"), missingFruits.suggestedItems.map { it.name })
    }

    @Test
    fun `prefers unprocessed and lower sodium or added sugar items when suggesting`() {
        val items = listOf<FoodItem>()
        val catalog = listOf(
            foodItem("Papas fritas", FoodGroup.VEGETABLES, isProcessed = true),
            foodItem("Sopa enlatada", FoodGroup.VEGETABLES, sodiumMg = 500.0),
            foodItem("Zanahoria", FoodGroup.VEGETABLES)
        )

        val report = checker.evaluate(items, catalog)

        val missingVegetables = report.missingFoodGroups.first { it.foodGroup == FoodGroup.VEGETABLES }
        assertEquals(
            listOf("Zanahoria", "Sopa enlatada", "Papas fritas"),
            missingVegetables.suggestedItems.map { it.name }
        )
    }

    @Test
    fun `caps suggestions at the maximum per group even if more servings are missing`() {
        val items = listOf<FoodItem>()
        val catalog = (1..DietaryGuidelinesChecker.MAX_SUGGESTIONS_PER_GROUP + 2)
            .map { foodItem("Fruta catalogo $it", FoodGroup.FRUITS) }

        val report = checker.evaluate(items, catalog)

        val missingFruits = report.missingFoodGroups.first { it.foodGroup == FoodGroup.FRUITS }
        assertEquals(DietaryGuidelinesChecker.MAX_SUGGESTIONS_PER_GROUP, missingFruits.suggestedItems.size)
    }

    @Test
    fun `does not suggest more items than the missing servings for a group`() {
        val recommended = DietaryGuidelinesChecker.RECOMMENDED_WEEKLY_SERVINGS.getValue(FoodGroup.FRUITS)
        val items = (1 until recommended).map { foodItem("Fruta $it", FoodGroup.FRUITS) }
        val catalog = listOf(foodItem("Banana", FoodGroup.FRUITS), foodItem("Kiwi", FoodGroup.FRUITS))

        val report = checker.evaluate(items, catalog)

        val missingFruits = report.missingFoodGroups.first { it.foodGroup == FoodGroup.FRUITS }
        assertEquals(1, missingFruits.missingServings)
        assertEquals(1, missingFruits.suggestedItems.size)
    }

    @Test
    fun `is compliant only when there are no missing food groups, no flagged items and sodium is within limit`() {
        val items = DietaryGuidelinesChecker.RECOMMENDED_WEEKLY_SERVINGS.flatMap { (foodGroup, recommended) ->
            (1..recommended).map { foodItem("$foodGroup $it", foodGroup, sodiumMg = 10.0) }
        }

        val report = checker.evaluate(items)

        assertTrue(report.missingFoodGroups.isEmpty())
        assertTrue(report.flaggedItems.isEmpty())
        assertFalse(report.exceedsDailySodiumLimit)
        assertTrue(report.isCompliant)
    }
}
