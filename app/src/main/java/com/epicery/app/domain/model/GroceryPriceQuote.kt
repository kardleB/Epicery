package com.epicery.app.domain.model

/**
 * Cotización de precio de un artículo en un supermercado, obtenida vía GroceryPulse
 * (Apify - Canadian Grocery Price Comparison API) para comparación de precios en
 * Montreal (RF3, RF5, CA4).
 */
data class GroceryPriceQuote(
    val storeName: String,
    val productName: String,
    val price: Double,
    val currency: String = "CAD",
    val city: String,
    val sourceUrl: String? = null
)
