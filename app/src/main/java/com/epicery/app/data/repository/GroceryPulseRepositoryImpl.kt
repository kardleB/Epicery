package com.epicery.app.data.repository

import com.epicery.app.BuildConfig
import com.epicery.app.data.remote.GroceryPriceResponse
import com.epicery.app.data.remote.GroceryPulseApi
import com.epicery.app.data.remote.GroceryPulseRequest
import com.epicery.app.domain.model.GroceryPriceQuote
import com.epicery.app.domain.repository.GroceryPulseRepository
import com.epicery.app.util.Constants
import javax.inject.Inject

class GroceryPulseRepositoryImpl @Inject constructor(
    private val api: GroceryPulseApi
) : GroceryPulseRepository {

    override suspend fun compareMontrealPrices(query: String): List<GroceryPriceQuote> {
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
        return response
            .filter { it.city.isNullOrBlank() || it.city.contains(Constants.MONTREAL_CITY, ignoreCase = true) }
            .mapNotNull { it.toGroceryPriceQuoteOrNull() }
    }
}

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
