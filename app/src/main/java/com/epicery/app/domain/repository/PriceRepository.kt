package com.epicery.app.domain.repository

import com.epicery.app.domain.model.PriceHistory
import kotlinx.coroutines.flow.Flow

/**
 * Acceso al historico de precios de los alimentos. Encapsula el
 * `PriceHistoryDao` de Room para que los casos de uso del dominio no
 * dependan de detalles de Room.
 */
interface PriceRepository {
    fun getPriceHistoryForFoodItem(foodItemId: Long): Flow<List<PriceHistory>>
    suspend fun getLatestPrice(foodItemId: Long): PriceHistory?
    suspend fun savePriceHistory(priceHistory: PriceHistory): Long
}
