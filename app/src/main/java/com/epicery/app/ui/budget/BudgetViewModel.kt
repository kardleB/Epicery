package com.epicery.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.calculator.WeeklySpending
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.repository.GroceryRepository
import com.epicery.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> = combine(
        groceryRepository.getGroceryItems(),
        settingsRepository.userSettings.map { it.defaultWeeklyBudget }
    ) { items, budget ->
        buildUiState(items, budget, System.currentTimeMillis())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BudgetUiState()
    )

    private fun buildUiState(items: List<GroceryItem>, budget: Double, nowMillis: Long): BudgetUiState {
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
            monthlyProjection = weeklySpent * BudgetUiState.WEEKS_PER_MONTH
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
