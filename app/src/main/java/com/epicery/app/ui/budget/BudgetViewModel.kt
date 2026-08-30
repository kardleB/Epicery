package com.epicery.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.repository.GroceryRepository
import com.epicery.app.domain.usecase.GetWeeklyBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val getWeeklyBudgetUseCase: GetWeeklyBudgetUseCase
) : ViewModel() {

    private val weeklyBudget = MutableStateFlow(0.0)

    val uiState: StateFlow<BudgetUiState> = combine(
        groceryRepository.getGroceryItems(),
        weeklyBudget
    ) { items, budget ->
        buildUiState(items, budget)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = BudgetUiState()
    )

    init {
        viewModelScope.launch {
            weeklyBudget.value = getWeeklyBudgetUseCase(ACTIVE_SHOPPING_LIST_ID)
        }
    }

    private fun buildUiState(items: List<GroceryItem>, budget: Double): BudgetUiState {
        val purchasedItems = items.filter { it.isPurchased }
        val weeklySpent = purchasedItems.sumOf { it.estimatedPrice }
        val spendingByCategory = purchasedItems
            .groupBy { item -> FoodGroup.entries.find { it.name.equals(item.foodGroup, ignoreCase = true) } }
            .mapNotNull { (group, groupItems) -> group?.let { it to groupItems.sumOf { item -> item.estimatedPrice } } }
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
        const val ACTIVE_SHOPPING_LIST_ID = 1L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
