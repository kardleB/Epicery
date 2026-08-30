package com.epicery.app.domain.usecase

import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Devuelve los alimentos del catalogo que pertenecen a una categoria dada. */
class GetFoodItemsByCategoryUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(category: String): Flow<List<FoodItem>> =
        foodRepository.getFoodItemsByCategory(category)
}
