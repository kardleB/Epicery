package com.epicery.app.domain.model

import com.epicery.app.data.local.FoodGroup

/**
 * Resultado de [com.epicery.app.domain.calculator.DietaryGuidelinesChecker.evaluate]: cumplimiento
 * de una lista de compras contra las Dietary Guidelines 2025-2030 (RF1, CA1).
 */
data class DietaryGuidelinesReport(
    val missingFoodGroups: List<MissingFoodGroup>,
    val flaggedItems: List<FlaggedFoodItem>,
    val totalSodiumMg: Double,
    val exceedsDailySodiumLimit: Boolean
) {
    val isCompliant: Boolean
        get() = missingFoodGroups.isEmpty() && flaggedItems.isEmpty() && !exceedsDailySodiumLimit
}

/** Grupo alimenticio con menos porciones en la lista que las recomendadas semanalmente. */
data class MissingFoodGroup(
    val foodGroup: FoodGroup,
    val presentServings: Int,
    val recommendedServings: Int
) {
    val missingServings: Int
        get() = (recommendedServings - presentServings).coerceAtLeast(0)
}

/** Motivo por el que un [FoodItem] de la lista de compras fue marcado al evaluarlo. */
enum class DietaryFlagReason {
    HIGHLY_PROCESSED,
    EXCESS_SODIUM,
    EXCESS_ADDED_SUGAR
}

/** [FoodItem] de la lista de compras marcado por incumplir una o mas guias alimenticias. */
data class FlaggedFoodItem(
    val itemName: String,
    val reasons: List<DietaryFlagReason>
)
