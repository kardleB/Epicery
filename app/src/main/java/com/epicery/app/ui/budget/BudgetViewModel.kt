package com.epicery.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.calculator.MonthlySpending
import com.epicery.app.domain.calculator.WeeklyExpenses
import com.epicery.app.domain.calculator.WeeklySpending
import com.epicery.app.domain.model.Expense
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.repository.ExpenseRepository
import com.epicery.app.domain.repository.GroceryRepository
import com.epicery.app.domain.repository.MonthlyBudgetRepository
import com.epicery.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/**
 * Pantalla de Budget (RF4): junta lo ya terminado ("Terminar compra") con lo recien marcado como
 * comprado en la lista activa, como `Flow`, para que el dashboard refleje en tiempo real el gasto
 * del mes, de la semana y su desglose por semana (CA3) frente a los presupuestos del usuario.
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    groceryRepository: GroceryRepository,
    expenseRepository: ExpenseRepository,
    settingsRepository: SettingsRepository,
    private val monthlyBudgetRepository: MonthlyBudgetRepository
) : ViewModel() {

    /** Mes mostrado, fijado al abrir la pantalla (no cambia solo si la app queda abierta al cambiar de mes). */
    private val month: YearMonth = MonthlySpending.monthOf(System.currentTimeMillis())

    val uiState: StateFlow<BudgetUiState> = combine(
        groceryRepository.getGroceryItems(),
        expenseRepository.observeExpenses(),
        settingsRepository.userSettings.map { it.defaultWeeklyBudget },
        monthlyBudgetRepository.observeBudget(month),
        monthlyBudgetRepository.observePreviousBudget(month)
    ) { items, expenses, budget, monthlyBudget, previousMonthlyBudget ->
        buildUiState(
            items, expenses, budget, monthlyBudget ?: 0.0, previousMonthlyBudget ?: 0.0, System.currentTimeMillis()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BudgetUiState()
    )

    /** Define o cambia el presupuesto del mes mostrado; ignora montos que no sean mayores que cero. */
    fun saveMonthlyBudget(amount: Double) {
        if (!amount.isFinite() || amount <= 0.0) return
        viewModelScope.launch { monthlyBudgetRepository.setBudget(month, amount) }
    }

    private fun buildUiState(
        items: List<GroceryItem>,
        expenses: List<Expense>,
        budget: Double,
        monthlyBudget: Double,
        previousMonthlyBudget: Double,
        nowMillis: Long
    ): BudgetUiState {
        val entries = WeeklyExpenses.entries(items, expenses, nowMillis)
        val weeks = WeeklyExpenses.groupByWeek(entries, month)
        val monthlySpent = MonthlySpending.inMonth(entries, month).sumOf { it.amount }
        val weekEntries = WeeklySpending.inWeek(entries, nowMillis)
        val weeklySpent = weekEntries.sumOf { it.amount }
        val spendingByCategory = weekEntries
            .groupBy { entry -> FoodGroup.entries.find { it.name.equals(entry.foodGroup, ignoreCase = true) } }
            .mapNotNull { (group, groupEntries) -> group?.let { it to groupEntries.sumOf { entry -> entry.amount } } }
            .toMap()
        return BudgetUiState(
            isLoading = false,
            weeklyBudget = budget,
            weeklySpent = weeklySpent,
            spendingByCategory = spendingByCategory,
            monthlyProjection = weeklySpent * BudgetUiState.WEEKS_PER_MONTH,
            month = month,
            monthlyBudget = monthlyBudget,
            monthlySpent = monthlySpent,
            previousMonthlyBudget = previousMonthlyBudget,
            weeks = weeks
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
