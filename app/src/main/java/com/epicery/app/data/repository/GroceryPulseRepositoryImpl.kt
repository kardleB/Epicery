package com.epicery.app.data.repository

import com.epicery.app.BuildConfig
import com.epicery.app.data.local.GroceryPriceCacheDao
import com.epicery.app.data.local.GroceryPriceCacheEntity
import com.epicery.app.data.remote.GroceryPriceResponse
import com.epicery.app.data.remote.GroceryPulseApi
import com.epicery.app.data.remote.GroceryPulseRequest
import com.epicery.app.domain.model.GroceryPriceQuote
import com.epicery.app.domain.repository.GroceryPulseRepository
import com.epicery.app.util.Constants
import java.io.IOException
import javax.inject.Inject

/**
 * Cachea las cotizaciones de GroceryPulse (Apify) en Room, indexadas por término de
 * búsqueda (RNF5): si la cache está vigente evita la llamada de red, y si la llamada
 * falla por falta de conexión devuelve la última respuesta cacheada en vez de fallar.
 */
class GroceryPulseRepositoryImpl @Inject constructor(
    private val api: GroceryPulseApi,
    private val cacheDao: GroceryPriceCacheDao
) : GroceryPulseRepository {

    override suspend fun compareMontrealPrices(query: String): List<GroceryPriceQuote> {
        val normalizedQuery = query.trim().lowercase()
        val cached = cacheDao.getByQuery(normalizedQuery)
        val cacheIsFresh = cached.isNotEmpty() &&
            System.currentTimeMillis() - cached.first().fetchedAt < Constants.API_CACHE_TTL_MS
        if (cacheIsFresh) {
            return cached.map { it.toGroceryPriceQuote() }
        }

        return try {
            check(BuildConfig.APIFY_API_TOKEN.isNotBlank()) {
                "Falta configurar APIFY_API_TOKEN en local.properties (ver README)."
            }
            check(BuildConfig.APIFY_GROCERY_ACTOR_ID.isNotBlank()) {
                "Falta configurar APIFY_GROCERY_ACTOR_ID en local.properties (ver README)."
            }
            val response = api.compareGroceryPrices(
                actorId = BuildConfig.APIFY_GROCERY_ACTOR_ID,
                token = BuildConfig.APIFY_API_TOKEN,
                request = GroceryPulseRequest(query = query, city = Constants.MONTREAL_CITY)
            )
            val quotes = response
                .filter { it.city.isNullOrBlank() || it.city.contains(Constants.MONTREAL_CITY, ignoreCase = true) }
                .mapNotNull { it.toGroceryPriceQuoteOrNull() }
            val fetchedAt = System.currentTimeMillis()
            cacheDao.replaceForQuery(normalizedQuery, quotes.map { it.toCacheEntity(normalizedQuery, fetchedAt) })
            quotes
        } catch (e: IOException) {
            if (cached.isNotEmpty()) cached.map { it.toGroceryPriceQuote() } else throw e
        }
    }
}

private fun GroceryPriceCacheEntity.toGroceryPriceQuote() = GroceryPriceQuote(
    storeName = storeName,
    productName = productName,
    price = price,
    currency = currency,
    city = city,
    sourceUrl = sourceUrl
)

private fun GroceryPriceQuote.toCacheEntity(normalizedQuery: String, fetchedAt: Long) = GroceryPriceCacheEntity(
    query = normalizedQuery,
    storeName = storeName,
    productName = productName,
    price = price,
    currency = currency,
    city = city,
    sourceUrl = sourceUrl,
    fetchedAt = fetchedAt
)

private fun GroceryPriceResponse.toGroceryPriceQuoteOrNull(): GroceryPriceQuote? {
    val storeName = store?.takeIf { it.isNotBlank() } ?: return null
    if (price <= 0.0) return null
    return GroceryPriceQuote(
        storeName = storeName,
        productName = title.orEmpty(),
        price = price,
        currency = currency?.takeIf { it.isNotBlank() } ?: "CAD",
        city = city?.takeIf { it.isNotBlank() } ?: Constants.MONTREAL_CITY,
        sourceUrl = url
    )
}
