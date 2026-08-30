package com.epicery.app.domain.repository

import com.epicery.app.domain.model.GroceryPriceQuote

/**
 * Acceso a GroceryPulse (Apify - Canadian Grocery Price Comparison API), RF3/RF5/CA4:
 * compara precios de un artículo en supermercados de Montreal.
 */
interface GroceryPulseRepository {
    suspend fun compareMontrealPrices(query: String): List<GroceryPriceQuote>
}
