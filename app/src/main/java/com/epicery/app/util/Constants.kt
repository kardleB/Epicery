package com.epicery.app.util

object Constants {
    const val DATABASE_NAME = "epicery.db"
    const val BASE_URL = "https://api.epicery.app/"

    /** USDA FoodData Central (RF1, CA1): límite gratuito de 1000 requests/hora por API key. */
    const val USDA_BASE_URL = "https://api.nal.usda.gov/fdc/v1/"

    /**
     * GroceryPulse (RF3, RF5, CA4): host de la API REST de Apify, usado para correr el
     * actor "Canadian Grocery Price Comparison" (ver `GroceryPulseApi`).
     */
    const val APIFY_BASE_URL = "https://api.apify.com/v2/"

    /** Ciudad usada para comparar precios de supermercados con GroceryPulse (RF3, RF5, CA4). */
    const val MONTREAL_CITY = "Montreal"
}
