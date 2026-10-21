package com.epicery.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.calculator.WeeklyExpenses
import com.epicery.app.domain.calculator.WeeklySpending
import com.epicery.app.domain.model.Expense
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.repository.ExpenseRepository
import com.epicery.app.domain.repository.GroceryRepository
import com.epicery.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groceryRepository: GroceryRepository,
    expenseRepository: ExpenseRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        groceryRepository.getGroceryItems(),
        expenseRepository.observeExpenses(),
        settingsRepository.userSettings.map { it.defaultWeeklyBudget }
    ) { items, expenses, budget ->
        buildUiState(items, expenses, budget, System.currentTimeMillis())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    /** Alta rápida desde el FAB de Home (ver `docs/design/wireframes.md`, sección "1. Home"). */
    fun addQuickItem(name: String, foodGroup: FoodGroup, estimatedPrice: Double) {
        viewModelScope.launch {
            groceryRepository.addGroceryItem(
                GroceryItem(name = name, foodGroup = foodGroup.name, estimatedPrice = estimatedPrice)
            )
        }
    }

    private fun buildUiState(
        items: List<GroceryItem>,
        expenses: List<Expense>,
        budget: Double,
        nowMillis: Long
    ): HomeUiState {
        val countsByGroup = FoodGroup.entries.associateWith { group ->
            items.count { it.foodGroup.equals(group.name, ignoreCase = true) }
        }
        val spentThisWeek = WeeklySpending
            .inWeek(WeeklyExpenses.entries(items, expenses, nowMillis), nowMillis)
            .sumOf { it.amount }
        return HomeUiState(
            isLoading = false,
            hasActiveList = items.isNotEmpty(),
            weeklyBudget = budget,
            amountSpent = spentThisWeek,
            shoppingListPreview = items.take(SHOPPING_LIST_PREVIEW_LIMIT),
            foodGroupCounts = countsByGroup
        )
    }

    private companion object {
        const val SHOPPING_LIST_PREVIEW_LIMIT = 5
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
