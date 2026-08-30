package com.epicery.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Cliente Retrofit de USDA FoodData Central (RF1, CA1): busca alimentos por nombre
 * y devuelve su información nutricional (calorías, proteína, sodio, azúcar, entre otros).
 * Requiere una API key gratuita (https://fdc.nal.usda.gov/api-key-signup.html), con un
 * límite de 1000 requests/hora por key — ver `Constants.USDA_BASE_URL` y `BuildConfig.USDA_API_KEY`.
 */
interface UsdaFoodDataApi {
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("pageSize") pageSize: Int = 1
    ): UsdaFoodSearchResponse
}
