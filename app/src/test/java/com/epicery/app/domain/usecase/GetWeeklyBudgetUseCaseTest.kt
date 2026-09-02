package com.epicery.app.domain.usecase

import com.epicery.app.domain.repository.BudgetRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifica que [GetWeeklyBudgetUseCase] (estimacion de presupuesto semanal, RF4) delegue
 * correctamente en [BudgetRepository] y devuelva el monto tal cual lo expone el repositorio,
 * aqui reemplazado por un fake en memoria para poder correr como test de JVM.
 */
class GetWeeklyBudgetUseCaseTest {

    private class FakeBudgetRepository(
        private val budgetsByListId: MutableMap<Long, Double> = mutableMapOf()
    ) : BudgetRepository {
        override suspend fun getWeeklyBudget(shoppingListId: Long): Double =
            budgetsByListId[shoppingListId] ?: 0.0

        override suspend fun setWeeklyBudget(shoppingListId: Long, amount: Double) {
            budgetsByListId[shoppingListId] = amount
        }

        override suspend fun syncWeeklyBudget(userId: String, shoppingListId: Long, amount: Double): Result<Unit> =
            Result.success(Unit)

        override suspend fun fetchRemoteWeeklyBudget(userId: String, shoppingListId: Long): Double? = null
    }

    @Test
    fun `returns the weekly budget configured for the shopping list`() = runBlocking {
        val useCase = GetWeeklyBudgetUseCase(FakeBudgetRepository(mutableMapOf(1L to 150.0)))

        val budget = useCase(shoppingListId = 1L)

        assertEquals(150.0, budget, 0.0001)
    }

    @Test
    fun `returns zero when the shopping list has no budget configured`() = runBlocking {
        val useCase = GetWeeklyBudgetUseCase(FakeBudgetRepository())

        val budget = useCase(shoppingListId = 99L)

        assertEquals(0.0, budget, 0.0001)
    }

    @Test
    fun `does not mix up budgets between different shopping lists`() = runBlocking {
        val useCase = GetWeeklyBudgetUseCase(FakeBudgetRepository(mutableMapOf(1L to 150.0, 2L to 80.0)))

        assertEquals(150.0, useCase(shoppingListId = 1L), 0.0001)
        assertEquals(80.0, useCase(shoppingListId = 2L), 0.0001)
    }
}
