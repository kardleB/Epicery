package com.epicery.app.data.repository

import com.epicery.app.data.local.PriceHistoryDao
import com.epicery.app.data.local.PriceHistoryEntity
import com.epicery.app.domain.model.PriceHistory
import com.epicery.app.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val dao: PriceHistoryDao
) : PriceRepository {

    override fun getPriceHistoryForFoodItem(foodItemId: Long): Flow<List<PriceHistory>> =
        dao.getHistoryForFoodItem(foodItemId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getLatestPrice(foodItemId: Long): PriceHistory? =
        dao.getLatestPriceForFoodItem(foodItemId)?.toDomain()

    override suspend fun savePriceHistory(priceHistory: PriceHistory): Long =
        dao.insert(priceHistory.toEntity())
}

private fun PriceHistoryEntity.toDomain() = PriceHistory(
    id = id,
    foodItemId = foodItemId,
    storeName = storeName,
    price = price,
    currency = currency,
    recordedAt = recordedAt
)

private fun PriceHistory.toEntity() = PriceHistoryEntity(
    id = id,
    foodItemId = foodItemId,
    storeName = storeName,
    price = price,
    currency = currency,
    recordedAt = recordedAt
)
