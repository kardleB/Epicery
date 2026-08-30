package com.epicery.app.domain.model

/**
 * Registro de precio de un [FoodItem] en un comercio, tal como lo consume
 * la capa de dominio.
 */
data class PriceHistory(
    val id: Long = 0,
    val foodItemId: Long,
    val storeName: String,
    val price: Double,
    val currency: String = "CAD",
    val recordedAt: Long
)
