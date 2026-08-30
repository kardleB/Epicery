package com.epicery.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Cliente Retrofit de GroceryPulse — el actor de Apify "Canadian Grocery Price
 * Comparison" (RF3, RF5, CA4): compara precios de un artículo en supermercados de
 * Montreal. Corre el actor de forma síncrona (`run-sync-get-dataset-items`), que ejecuta
 * el scraping y devuelve directamente los items de su dataset de salida en la misma
 * respuesta, sin necesidad de sondear el estado del run por separado.
 *
 * Requiere una cuenta de Apify (https://apify.com) con:
 *  - un token de API (`BuildConfig.APIFY_API_TOKEN`), y
 *  - el ID/slug del actor suscrito para esta API (`BuildConfig.APIFY_GROCERY_ACTOR_ID`),
 * ambos configurados en `local.properties` (ver README), ya que el ID de actor puede
 * variar según el plan/suscripción de Apify de cada desarrollador.
 */
interface GroceryPulseApi {
    @POST("acts/{actorId}/run-sync-get-dataset-items")
    suspend fun compareGroceryPrices(
        @Path("actorId") actorId: String,
        @Query("token") token: String,
        @Body request: GroceryPulseRequest
    ): List<GroceryPriceResponse>
}
