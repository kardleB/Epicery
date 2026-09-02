package com.epicery.app.ui.shoppinglist

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
class ShoppingListViewModel @Inject constructor(
    private val groceryRepository: GroceryRepository,
    private val getWeeklyBudgetUseCase: GetWeeklyBudgetUseCase
) : ViewModel() {

    private val weeklyBudget = MutableStateFlow(0.0)
    private val selectedFoodGroup = MutableStateFlow<FoodGroup?>(null)

    val uiState: StateFlow<ShoppingListUiState> = combine(
        groceryRepository.getGroceryItems(),
        weeklyBudget,
        selectedFoodGroup
    ) { items, budget, selectedGroup ->
        buildUiState(items, budget, selectedGroup)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ShoppingListUiState()
    )

    init {
        viewModelScope.launch {
            weeklyBudget.value = getWeeklyBudgetUseCase(ACTIVE_SHOPPING_LIST_ID)
        }
    }

    /** Filtra la lista por grupo alimenticio (CA1); `null` vuelve a mostrar todos los grupos. */
    fun selectFoodGroup(foodGroup: FoodGroup?) {
        selectedFoodGroup.value = foodGroup
    }

    /** Alta de un item nuevo (CA1: "el usuario puede crear una lista"). */
    fun addItem(name: String, foodGroup: FoodGroup, estimatedPrice: Double) {
        viewModelScope.launch {
            groceryRepository.addGroceryItem(
                GroceryItem(name = name, foodGroup = foodGroup.name, estimatedPrice = estimatedPrice)
            )
        }
    }

    /** Marca/desmarca un item como comprado (CA1). */
    fun togglePurchased(item: GroceryItem) {
        viewModelScope.launch {
            groceryRepository.updateGroceryItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

    /** Edita el precio estimado de un item ya existente. */
    fun updatePrice(item: GroceryItem, newPrice: Double) {
        viewModelScope.launch {
            groceryRepository.updateGroceryItem(item.copy(estimatedPrice = newPrice))
        }
    }

    private fun buildUiState(
        items: List<GroceryItem>,
        budget: Double,
        selectedGroup: FoodGroup?
    ): ShoppingListUiState {
        val visibleItems = if (selectedGroup == null) {
            items
        } else {
            items.filter { it.foodGroup.equals(selectedGroup.name, ignoreCase = true) }
        }
        val itemsByGroup = FoodGroup.entries.associateWith { group ->
            visibleItems.filter { it.foodGroup.equals(group.name, ignoreCase = true) }
        }
        return ShoppingListUiState(
            isLoading = false,
            weeklyBudget = budget,
            totalEstimated = items.sumOf { it.estimatedPrice },
            selectedFoodGroup = selectedGroup,
            itemsByGroup = itemsByGroup,
            allItems = items
        )
    }

    private companion object {
        const val ACTIVE_SHOPPING_LIST_ID = 1L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
