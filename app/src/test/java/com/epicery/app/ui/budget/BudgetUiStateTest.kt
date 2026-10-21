package com.epicery.app.ui.budget

import com.epicery.app.domain.calculator.MonthlyBudgetStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica las cifras derivadas que muestra la tarjeta del presupuesto mensual. */
class BudgetUiStateTest {

    @Test
    fun `without a monthly budget nothing is computed against it`() {
        val state = BudgetUiState(monthlySpent = 120.0, monthlyProjection = 500.0)

        assertFalse(state.isMonthlyBudgetSet)
        assertEquals(0f, state.monthlyProgress, 0.0001f)
        assertEquals(MonthlyBudgetStatus.NOT_SET, state.monthlyStatus)
        assertFalse(state.isOverMonthlyProjection)
    }

    @Test
    fun `progress, percent and remaining follow the spending`() {
        val state = BudgetUiState(monthlyBudget = 400.0, monthlySpent = 330.0)

        assertEquals(0.825f, state.monthlyProgress, 0.0001f)
        assertEquals(83, state.monthlyPercentUsed)
        assertEquals(70.0, state.monthlyRemaining, 0.0001)
        assertEquals(MonthlyBudgetStatus.WARNING, state.monthlyStatus)
    }

    @Test
    fun `progress bar is capped at 100 percent but the percent keeps growing`() {
        val state = BudgetUiState(monthlyBudget = 100.0, monthlySpent = 150.0)

        assertEquals(1f, state.monthlyProgress, 0.0001f)
        assertEquals(150, state.monthlyPercentUsed)
        assertEquals(-50.0, state.monthlyRemaining, 0.0001)
        assertEquals(MonthlyBudgetStatus.OVER, state.monthlyStatus)
    }

    @Test
    fun `projection is compared against the budget the user defined`() {
        assertTrue(BudgetUiState(monthlyBudget = 200.0, monthlyProjection = 250.0).isOverMonthlyProjection)
        assertFalse(BudgetUiState(monthlyBudget = 300.0, monthlyProjection = 250.0).isOverMonthlyProjection)
    }

    @Test
    fun `suggested monthly budget comes from the weekly one`() {
        assertEquals(0.0, BudgetUiState(weeklyBudget = 0.0).suggestedMonthlyBudget, 0.0001)
        assertEquals(391.0, BudgetUiState(weeklyBudget = 90.0).suggestedMonthlyBudget, 0.0001)
    }
}
