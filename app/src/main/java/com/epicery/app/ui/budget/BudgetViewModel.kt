package com.epicery.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.calculator.MonthlySpending
import com.epicery.app.domain.calculator.WeeklySpending
import com.epicery.app.domain.model.GroceryItem
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
 * Pantalla de Budget (RF4): observa la misma lista activa que Shopping List
 * ([ShoppingListViewModel][com.epicery.app.ui.shoppinglist.ShoppingListViewModel]) como `Flow`
 * para que el dashboard refleje en tiempo real el gasto acumulado (items ya comprados) contra el
 * presupuesto semanal configurado por el usuario, y calcula la proyección de gasto mensual (CA3).
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    groceryRepository: GroceryRepository,
    settingsRepository: SettingsRepository,
    private val monthlyBudgetRepository: MonthlyBudgetRepository
) : ViewModel() {

    /** Mes mostrado, fijado al abrir la pantalla (no cambia solo si la app queda abierta al cambiar de mes). */
    private val month: YearMonth = MonthlySpending.monthOf(System.currentTimeMillis())

    val uiState: StateFlow<BudgetUiState> = combine(
        groceryRepository.getGroceryItems(),
        settingsRepository.userSettings.map { it.defaultWeeklyBudget },
        monthlyBudgetRepository.observeBudget(month),
        monthlyBudgetRepository.observePreviousBudget(month)
    ) { items, budget, monthlyBudget, previousMonthlyBudget ->
        buildUiState(items, budget, monthlyBudget ?: 0.0, previousMonthlyBudget ?: 0.0, System.currentTimeMillis())
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
        budget: Double,
        monthlyBudget: Double,
        previousMonthlyBudget: Double,
        nowMillis: Long
    ): BudgetUiState {
        val monthlySpent = MonthlySpending.purchasedInMonth(items, month, nowMillis).sumOf { it.spentAmount }
        val purchasedItems = WeeklySpending.purchasedThisWeek(items, nowMillis)
        val weeklySpent = purchasedItems.sumOf { it.spentAmount }
        val spendingByCategory = purchasedItems
            .groupBy { item -> FoodGroup.entries.find { it.name.equals(item.foodGroup, ignoreCase = true) } }
            .mapNotNull { (group, groupItems) -> group?.let { it to groupItems.sumOf { item -> item.spentAmount } } }
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
            previousMonthlyBudget = previousMonthlyBudget
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
