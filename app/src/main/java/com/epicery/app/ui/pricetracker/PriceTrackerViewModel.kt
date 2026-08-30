package com.epicery.app.ui.pricetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.model.PriceHistory
import com.epicery.app.domain.repository.FoodRepository
import com.epicery.app.domain.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PriceTrackerViewModel @Inject constructor(
    foodRepository: FoodRepository,
    priceRepository: PriceRepository
) : ViewModel() {

    private val selectedFoodItemId = MutableStateFlow<Long?>(null)

    private val selectedPriceHistory = selectedFoodItemId.flatMapLatest { foodItemId ->
        if (foodItemId == null) flowOf(emptyList()) else priceRepository.getPriceHistoryForFoodItem(foodItemId)
    }

    val uiState: StateFlow<PriceTrackerUiState> = combine(
        foodRepository.getFoodItems(),
        selectedFoodItemId,
        selectedPriceHistory
    ) { foodItems, selectedId, history ->
        buildUiState(foodItems, selectedId, history)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = PriceTrackerUiState()
    )

    /** Selecciona el artículo a graficar (CA2: "al seleccionar un artículo con histórico"). */
    fun selectFoodItem(foodItem: FoodItem) {
        selectedFoodItemId.value = foodItem.id
    }

    private fun buildUiState(
        foodItems: List<FoodItem>,
        selectedId: Long?,
        history: List<PriceHistory>
    ): PriceTrackerUiState {
        val selectedFoodItem = foodItems.find { it.id == selectedId }
        val chronologicalHistory = history.sortedBy { it.recordedAt }
        val averagePrice = if (chronologicalHistory.isNotEmpty()) {
            chronologicalHistory.map { it.price }.average()
        } else {
            0.0
        }
        return PriceTrackerUiState(
            isLoading = false,
            foodItems = foodItems,
            selectedFoodItem = selectedFoodItem,
            priceHistory = chronologicalHistory,
            averagePrice = averagePrice,
            latestPrice = chronologicalHistory.lastOrNull()?.price,
            trend = computeTrend(chronologicalHistory)
        )
    }

    /** Compara el último precio contra el promedio de los registros anteriores; una banda de
     * tolerancia del [TREND_THRESHOLD] evita marcar como "sube"/"baja" variaciones insignificantes. */
    private fun computeTrend(chronologicalHistory: List<PriceHistory>): PriceTrend {
        if (chronologicalHistory.size < 2) return PriceTrend.STABLE
        val latestPrice = chronologicalHistory.last().price
        val previousAveragePrice = chronologicalHistory.dropLast(1).map { it.price }.average()
        if (previousAveragePrice <= 0.0) return PriceTrend.STABLE
        val changeRatio = (latestPrice - previousAveragePrice) / previousAveragePrice
        return when {
            changeRatio > TREND_THRESHOLD -> PriceTrend.UP
            changeRatio < -TREND_THRESHOLD -> PriceTrend.DOWN
            else -> PriceTrend.STABLE
        }
    }

    private companion object {
        const val TREND_THRESHOLD = 0.02
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
