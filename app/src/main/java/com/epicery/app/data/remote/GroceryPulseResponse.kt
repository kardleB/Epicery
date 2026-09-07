package com.epicery.app.data.remote

import com.epicery.app.util.Constants

/**
 * Input del actor de Apify usado por GroceryPulse (RF3, RF5, CA4): término de búsqueda y
 * código postal a comparar. Los nombres de campo coinciden con los que espera el actor
 * "Flipp Scraper" (`chimerical_quicklime/flipp-scraper`, ver README); si el equipo se
 * suscribe a un actor distinto, ajustar este data class a su esquema real.
 */
data class GroceryPulseRequest(
    val query: String,
    val postalCode: String = Constants.MONTREAL_POSTAL_CODE,
    val maxItems: Int = 20
)

/**
 * Item del dataset de salida del actor de Apify (uno por combinación artículo/comercio).
 * Los campos son nullable porque Gson puede instanciar estas data classes sin pasar por el
 * constructor (bypasseando los valores por defecto de Kotlin) cuando una clave falta en el
 * JSON, y porque el esquema de salida puede variar levemente entre versiones del actor.
 */
data class GroceryPriceResponse(
    val merchant: String? = null,
    val name: String? = null,
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val postalCode: String? = null,
    val url: String? = null
)
