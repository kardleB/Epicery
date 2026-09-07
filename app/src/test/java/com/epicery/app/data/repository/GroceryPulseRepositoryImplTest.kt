package com.epicery.app.data.repository

import com.epicery.app.data.local.GroceryPriceCacheDao
import com.epicery.app.data.local.GroceryPriceCacheEntity
import com.epicery.app.data.remote.ApiErrorState
import com.epicery.app.data.remote.ApiFailureReason
import com.epicery.app.data.remote.GroceryPriceResponse
import com.epicery.app.data.remote.GroceryPulseApi
import com.epicery.app.data.remote.GroceryPulseRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica que [GroceryPulseRepositoryImpl] nunca deja propagar una excepción cuando
 * GroceryPulse (Apify) no está disponible: en cambio degrada a la última cotización
 * cacheada, o a una lista vacía si tampoco hay cache, para que la app siga funcionando con
 * datos locales. [UnreachableGroceryPulseApi] lanza [IllegalStateException] (el mismo tipo
 * que dispara el `check()` real de `GroceryPulseRepositoryImpl` cuando faltan las
 * credenciales) para simular la falla de forma determinística, sin depender de si
 * `local.properties` tiene o no `APIFY_API_TOKEN`/`APIFY_GROCERY_ACTOR_ID` configurados en
 * la máquina que corre el test.
 */
class GroceryPulseRepositoryImplTest {

    private class UnreachableGroceryPulseApi : GroceryPulseApi {
        override suspend fun compareGroceryPrices(
            actorId: String,
            token: String,
            request: GroceryPulseRequest
        ): List<GroceryPriceResponse> {
            throw IllegalStateException("GroceryPulse no disponible (simulado en el test)")
        }
    }

    private class FakeGroceryPriceCacheDao(seed: List<GroceryPriceCacheEntity> = emptyList()) : GroceryPriceCacheDao {
        private var entries: List<GroceryPriceCacheEntity> = seed

        override suspend fun getByQuery(query: String): List<GroceryPriceCacheEntity> = entries

        override suspend fun insertAll(entries: List<GroceryPriceCacheEntity>) {
            this.entries = this.entries + entries
        }

        override suspend fun deleteByQuery(query: String) {
            entries = emptyList()
        }
    }

    @Test
    fun `falls back to cached quotes instead of throwing when the API is unavailable`() = runBlocking {
        val staleCache = listOf(
            GroceryPriceCacheEntity(
                id = 1,
                query = "leche",
                storeName = "Metro",
                productName = "Leche",
                price = 3.49,
                currency = "CAD",
                city = "Montreal",
                sourceUrl = null,
                fetchedAt = 0L // vencido, fuerza a intentar la red y caer al catch
            )
        )
        val repository = GroceryPulseRepositoryImpl(
            api = UnreachableGroceryPulseApi(),
            cacheDao = FakeGroceryPriceCacheDao(staleCache)
        )

        val result = repository.compareMontrealPrices("Leche")

        assertEquals(1, result.size)
        assertEquals("Metro", result.first().storeName)
    }

    @Test
    fun `returns an empty list instead of throwing when the API is unavailable and there is no cache`() = runBlocking {
        val repository = GroceryPulseRepositoryImpl(
            api = UnreachableGroceryPulseApi(),
            cacheDao = FakeGroceryPriceCacheDao()
        )

        val result = repository.compareMontrealPrices("ArtículoDesconocido")

        assertTrue(result.isEmpty())
        assertEquals(ApiFailureReason.CONFIG_ERROR, ApiErrorState.lastError.value?.reason)
    }
}
