package com.epicery.app.domain.usecase

import com.epicery.app.domain.repository.BudgetRepository
import javax.inject.Inject

/** Obtiene el presupuesto semanal configurado para una lista de compras. */
class GetWeeklyBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(shoppingListId: Long): Double =
        budgetRepository.getWeeklyBudget(shoppingListId)
}
