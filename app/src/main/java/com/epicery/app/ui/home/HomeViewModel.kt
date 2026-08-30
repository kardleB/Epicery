package com.epicery.app.ui.home

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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groceryRepository: GroceryRepository,
    private val getWeeklyBudgetUseCase: GetWeeklyBudgetUseCase
) : ViewModel() {

    private val weeklyBudget = MutableStateFlow(0.0)

    val uiState: StateFlow<HomeUiState> = combine(
        groceryRepository.getGroceryItems(),
        weeklyBudget
    ) { items, budget ->
        buildUiState(items, budget)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    init {
        // La app todavía no tiene selección de lista activa (RF a futuro); mientras tanto se
        // usa la primera lista de compras como "la" lista activa, igual que `GroceryRepository`
        // (que no distingue entre listas todavía).
        viewModelScope.launch {
            weeklyBudget.value = getWeeklyBudgetUseCase(ACTIVE_SHOPPING_LIST_ID)
        }
    }

    /** Alta rápida desde el FAB de Home (ver `docs/design/wireframes.md`, sección "1. Home"). */
    fun addQuickItem(name: String, foodGroup: FoodGroup, estimatedPrice: Double) {
        viewModelScope.launch {
            groceryRepository.addGroceryItem(
                GroceryItem(name = name, foodGroup = foodGroup.name, estimatedPrice = estimatedPrice)
            )
        }
    }

    private fun buildUiState(items: List<GroceryItem>, budget: Double): HomeUiState {
        val countsByGroup = FoodGroup.entries.associateWith { group ->
            items.count { it.foodGroup.equals(group.name, ignoreCase = true) }
        }
        return HomeUiState(
            isLoading = false,
            hasActiveList = items.isNotEmpty(),
            weeklyBudget = budget,
            amountSpent = items.sumOf { it.estimatedPrice },
            shoppingListPreview = items.take(SHOPPING_LIST_PREVIEW_LIMIT),
            foodGroupCounts = countsByGroup
        )
    }

    private companion object {
        const val SHOPPING_LIST_PREVIEW_LIMIT = 5
        const val ACTIVE_SHOPPING_LIST_ID = 1L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
