package com.epicery.app.domain.usecase

import com.epicery.app.domain.model.PriceHistory
import com.epicery.app.domain.repository.PriceRepository
import javax.inject.Inject

/** Registra un nuevo precio observado para un alimento en un comercio. */
class SavePriceHistoryUseCase @Inject constructor(
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(priceHistory: PriceHistory): Long =
        priceRepository.savePriceHistory(priceHistory)
}
