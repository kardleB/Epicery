package com.epicery.app.data.repository

import com.epicery.app.data.local.UsdaNutritionCacheDao
import com.epicery.app.data.local.UsdaNutritionCacheEntity
import com.epicery.app.data.remote.ApiErrorState
import com.epicery.app.data.remote.UsdaFoodDataApi
import com.epicery.app.data.remote.UsdaFoodSearchResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifica que [UsdaFoodDataRepositoryImpl] nunca deja propagar una excepción cuando USDA
 * FoodData no está disponible (en este entorno de test, `BuildConfig.USDA_API_KEY` está
 * vacío porque no hay `local.properties` — el mismo `check()` que dispara un fallo real de
 * configuración/API en producción): en cambio degrada a la última respuesta cacheada, o a
 * `null` si tampoco hay cache, para que la app siga funcionando con datos locales.
 */
class UsdaFoodDataRepositoryImplTest {

    private class UnreachableUsdaFoodDataApi : UsdaFoodDataApi {
        override suspend fun searchFoods(query: String, apiKey: String, pageSize: Int): UsdaFoodSearchResponse {
            throw AssertionError("no debería llamarse a la API sin una key configurada")
        }
    }

    private class FakeUsdaNutritionCacheDao(seed: UsdaNutritionCacheEntity? = null) : UsdaNutritionCacheDao {
        private var entry: UsdaNutritionCacheEntity? = seed

        override suspend fun upsert(entry: UsdaNutritionCacheEntity) {
            this.entry = entry
        }

        override suspend fun getByQuery(query: String): UsdaNutritionCacheEntity? = entry
    }

    @Test
    fun `falls back to cached nutrition instead of throwing when the API is unavailable`() = runBlocking {
        val staleCache = UsdaNutritionCacheEntity(
            query = "manzana",
            fdcId = 1L,
            description = "Manzana",
            calories = 52.0,
            proteinGrams = 0.3,
            sodiumMg = 1.0,
            sugarGrams = 10.0,
            fetchedAt = 0L // vencido, fuerza a intentar la red y caer al catch
        )
        val repository = UsdaFoodDataRepositoryImpl(
            api = UnreachableUsdaFoodDataApi(),
            cacheDao = FakeUsdaNutritionCacheDao(staleCache)
        )

        val result = repository.searchNutrition("Manzana")

        assertEquals(staleCache.description, result?.description)
        assertEquals(staleCache.calories, result?.calories)
    }

    @Test
    fun `returns null instead of throwing when the API is unavailable and there is no cache`() = runBlocking {
        val repository = UsdaFoodDataRepositoryImpl(
            api = UnreachableUsdaFoodDataApi(),
            cacheDao = FakeUsdaNutritionCacheDao(seed = null)
        )

        val result = repository.searchNutrition("ArtículoDesconocido")

        assertNull(result)
        assertEquals(
            com.epicery.app.data.remote.ApiFailureReason.CONFIG_ERROR,
            ApiErrorState.lastError.value?.reason
        )
    }
}
