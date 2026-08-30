package com.epicery.app.domain.usecase

import com.epicery.app.domain.model.GroceryPriceQuote
import com.epicery.app.domain.model.PriceHistory
import com.epicery.app.domain.repository.GroceryPulseRepository
import com.epicery.app.domain.repository.PriceRepository
import javax.inject.Inject

/**
 * Compara precios de un alimento en supermercados de Montreal vía GroceryPulse (Apify)
 * y persiste cada cotización como [PriceHistory] para el tracking de precios y la
 * estimación de presupuesto semanal (RF3, RF5, CA4). Si la API no está disponible o no
 * hay comercios de Montreal para la consulta, devuelve una lista vacía y no persiste
 * nada — comportamiento esperado de una comparación sin resultados, no un error.
 */
class GetMontrealGroceryPricesUseCase @Inject constructor(
    private val groceryPulseRepository: GroceryPulseRepository,
    private val priceRepository: PriceRepository
) {
    suspend operator fun invoke(foodItemId: Long, query: String): List<PriceHistory> {
        val recordedAt = System.currentTimeMillis()
        return groceryPulseRepository.compareMontrealPrices(query).map { quote ->
            val priceHistory = quote.toPriceHistory(foodItemId, recordedAt)
            val id = priceRepository.savePriceHistory(priceHistory)
            priceHistory.copy(id = id)
        }
    }
}

private fun GroceryPriceQuote.toPriceHistory(foodItemId: Long, recordedAt: Long) = PriceHistory(
    foodItemId = foodItemId,
    storeName = storeName,
    price = price,
    currency = currency,
    recordedAt = recordedAt
)
