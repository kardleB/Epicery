package com.epicery.app.data.remote

/**
 * Input del actor de Apify usado por GroceryPulse (RF3, RF5, CA4): término de búsqueda
 * y ciudad a comparar. Los nombres de campo siguen la convención común de los actores de
 * scraping de Apify (`query`/`city`); si el actor suscrito en `BuildConfig.APIFY_GROCERY_ACTOR_ID`
 * espera otros nombres, ajustar este data class.
 */
data class GroceryPulseRequest(
    val query: String,
    val city: String = "Montreal",
    val maxItems: Int = 20
)

/**
 * Item del dataset de salida del actor de Apify "Canadian Grocery Price Comparison"
 * (uno por combinación artículo/supermercado). Los campos son nullable porque Gson puede
 * instanciar estas data classes sin pasar por el constructor (bypasseando los valores por
 * defecto de Kotlin) cuando una clave falta en el JSON, y porque el esquema de salida
 * puede variar levemente entre versiones del actor.
 */
data class GroceryPriceResponse(
    val store: String? = null,
    val title: String? = null,
    val price: Double = 0.0,
    val currency: String? = null,
    val city: String? = null,
    val url: String? = null
)
