package com.epicery.app.domain.calculator

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.DietaryFlagReason
import com.epicery.app.domain.model.DietaryGuidelinesReport
import com.epicery.app.domain.model.FlaggedFoodItem
import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.model.MissingFoodGroup

/**
 * Verifica el cumplimiento de una lista de compras (los [FoodItem] del catalogo que la componen)
 * contra las Dietary Guidelines 2025-2030 (RF1, CA1): porciones semanales recomendadas por grupo
 * alimenticio, limite de sodio diario, azucares anadidos por comida y marcado de alimentos
 * altamente procesados.
 *
 * Es una clase de dominio pura (sin dependencias de Android ni de Room), en la misma linea que
 * [BudgetCalculator], para poder testearla como JVM test y reutilizarla desde use cases o ViewModels.
 */
class DietaryGuidelinesChecker {

    /**
     * Evalua [items] (los alimentos de una lista de compras) y devuelve un [DietaryGuidelinesReport]
     * indicando que grupos alimenticios tienen menos porciones que las recomendadas y que items
     * fueron marcados por ser altamente procesados o tener exceso de sodio o azucares anadidos.
     *
     * Si se provee [catalog] (por ejemplo, todo el catalogo de alimentos disponible), cada grupo
     * alimenticio faltante incluye ademas hasta [MAX_SUGGESTIONS_PER_GROUP] sugerencias de
     * [FoodItem] de ese grupo para completar la lista de compras, priorizando alimentos no
     * procesados y con menos sodio/azucares anadidos. Sin catalogo, las sugerencias quedan vacias.
     */
    fun evaluate(items: List<FoodItem>, catalog: List<FoodItem> = emptyList()): DietaryGuidelinesReport {
        val missingFoodGroups = RECOMMENDED_WEEKLY_SERVINGS.mapNotNull { (foodGroup, recommendedServings) ->
            val presentServings = items.count { it.foodGroup == foodGroup }
            if (presentServings < recommendedServings) {
                val missingServings = recommendedServings - presentServings
                MissingFoodGroup(
                    foodGroup = foodGroup,
                    presentServings = presentServings,
                    recommendedServings = recommendedServings,
                    suggestedItems = suggestItemsForGroup(foodGroup, missingServings, items, catalog)
                )
            } else {
                null
            }
        }

        val flaggedItems = items.mapNotNull { item ->
            val reasons = buildList {
                if (item.isProcessed) add(DietaryFlagReason.HIGHLY_PROCESSED)
                if (item.sodiumMg >= HIGH_SODIUM_PER_ITEM_MG) add(DietaryFlagReason.EXCESS_SODIUM)
                if (item.addedSugarGrams >= ADDED_SUGAR_LIMIT_PER_MEAL_G) add(DietaryFlagReason.EXCESS_ADDED_SUGAR)
            }
            if (reasons.isEmpty()) null else FlaggedFoodItem(item.name, reasons)
        }

        // Limite de sodio (<2300 mg/dia): se compara la suma de sodio de toda la lista contra
        // DAILY_SODIUM_LIMIT_MG, ya que ese limite es diario y no tiene sentido aplicarlo a un
        // unico item; el exceso de sodio por item se marca por separado con HIGH_SODIUM_PER_ITEM_MG.
        val totalSodiumMg = items.sumOf { it.sodiumMg }

        return DietaryGuidelinesReport(
            missingFoodGroups = missingFoodGroups,
            flaggedItems = flaggedItems,
            totalSodiumMg = totalSodiumMg,
            exceedsDailySodiumLimit = totalSodiumMg > DAILY_SODIUM_LIMIT_MG
        )
    }

    /**
     * Sugiere hasta [MAX_SUGGESTIONS_PER_GROUP] alimentos de [catalog] que pertenecen a
     * [foodGroup] para cubrir [missingServings], excluyendo los que ya estan en la lista de
     * compras ([itemsInList]) y priorizando los no procesados con menos sodio/azucares anadidos.
     */
    private fun suggestItemsForGroup(
        foodGroup: FoodGroup,
        missingServings: Int,
        itemsInList: List<FoodItem>,
        catalog: List<FoodItem>
    ): List<FoodItem> {
        val namesInList = itemsInList.mapTo(mutableSetOf()) { it.name }
        return catalog
            .asSequence()
            .filter { it.foodGroup == foodGroup && it.name !in namesInList }
            .sortedWith(compareBy({ it.isProcessed }, { it.sodiumMg }, { it.addedSugarGrams }))
            .take(minOf(missingServings, MAX_SUGGESTIONS_PER_GROUP))
            .toList()
    }

    companion object {
        /** Limite diario de sodio recomendado por las Dietary Guidelines 2025-2030. */
        const val DAILY_SODIUM_LIMIT_MG = 2300.0

        /** Azucares anadidos maximos recomendados por comida. */
        const val ADDED_SUGAR_LIMIT_PER_MEAL_G = 10.0

        /**
         * Umbral para marcar un item individual con exceso de sodio: 20% del limite diario
         * ([DAILY_SODIUM_LIMIT_MG]), el mismo criterio que usa la FDA para etiquetar un alimento
         * como "alto en sodio" por porcion.
         */
        const val HIGH_SODIUM_PER_ITEM_MG = DAILY_SODIUM_LIMIT_MG * 0.2

        /** Cantidad maxima de sugerencias de items que se devuelven por grupo alimenticio faltante. */
        const val MAX_SUGGESTIONS_PER_GROUP = 3

        /**
         * Porciones semanales recomendadas por grupo alimenticio: adaptacion a un conteo de items
         * (una porcion por item) de las porciones diarias del patron de referencia de 2000 kcal/dia
         * de las Dietary Guidelines 2025-2030, llevadas a una semana (x7) para poder compararlas
         * contra una lista de compras semanal.
         */
        val RECOMMENDED_WEEKLY_SERVINGS: Map<FoodGroup, Int> = mapOf(
            FoodGroup.FRUITS to 14,
            FoodGroup.VEGETABLES to 21,
            FoodGroup.GRAINS to 42,
            FoodGroup.PROTEIN to 14,
            FoodGroup.DAIRY to 21
        )
    }
}
