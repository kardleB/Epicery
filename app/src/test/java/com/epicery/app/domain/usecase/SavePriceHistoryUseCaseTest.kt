package com.epicery.app.domain.usecase

import com.epicery.app.data.local.PriceHistoryDao
import com.epicery.app.data.local.PriceHistoryEntity
import com.epicery.app.data.repository.PriceRepositoryImpl
import com.epicery.app.domain.model.PriceHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Verifica que [SavePriceHistoryUseCase] (tracking de precios, RF3) persista un [PriceHistory]
 * vía [PriceRepositoryImpl] (aqui reemplazado por un fake en memoria de [PriceHistoryDao] para
 * correr como test de JVM) y devuelva el id generado.
 */
class SavePriceHistoryUseCaseTest {

    private class FakePriceHistoryDao : PriceHistoryDao {
        private var nextId = 1L
        private val itemsFlow = MutableStateFlow(emptyList<PriceHistoryEntity>())

        override suspend fun insert(priceHistory: PriceHistoryEntity): Long {
            val withId = priceHistory.copy(id = nextId++)
            itemsFlow.value = itemsFlow.value + withId
            return withId.id
        }

        override suspend fun insertAll(priceHistories: List<PriceHistoryEntity>): List<Long> =
            priceHistories.map { insert(it) }

        override suspend fun update(priceHistory: PriceHistoryEntity) {
            itemsFlow.value = itemsFlow.value.map { if (it.id == priceHistory.id) priceHistory else it }
        }

        override suspend fun delete(priceHistory: PriceHistoryEntity) {
            itemsFlow.value = itemsFlow.value.filterNot { it.id == priceHistory.id }
        }

        override suspend fun getById(id: Long): PriceHistoryEntity? =
            itemsFlow.value.find { it.id == id }

        override fun getAll(): Flow<List<PriceHistoryEntity>> = itemsFlow

        override fun getHistoryForFoodItem(foodItemId: Long): Flow<List<PriceHistoryEntity>> =
            itemsFlow.map { items -> items.filter { it.foodItemId == foodItemId } }

        override suspend fun getLatestPriceForFoodItem(foodItemId: Long): PriceHistoryEntity? =
            itemsFlow.value.filter { it.foodItemId == foodItemId }.maxByOrNull { it.recordedAt }
    }

    private fun priceHistory(foodItemId: Long = 42, storeName: String = "Metro", price: Double = 3.49) =
        PriceHistory(foodItemId = foodItemId, storeName = storeName, price = price, recordedAt = 1_000L)

    @Test
    fun `persists a new price observation and returns its generated id`() = runBlocking {
        val dao = FakePriceHistoryDao()
        val useCase = SavePriceHistoryUseCase(PriceRepositoryImpl(dao))

        val id = useCase(priceHistory())

        assertNotEquals(0L, id)
        val saved = dao.getById(id)
        requireNotNull(saved)
        assertEquals("Metro", saved.storeName)
        assertEquals(3.49, saved.price, 0.0001)
    }

    @Test
    fun `accumulates multiple observations for the same food item as separate history entries`() = runBlocking {
        val dao = FakePriceHistoryDao()
        val useCase = SavePriceHistoryUseCase(PriceRepositoryImpl(dao))

        useCase(priceHistory(foodItemId = 42, storeName = "Metro", price = 3.49))
        useCase(priceHistory(foodItemId = 42, storeName = "IGA", price = 3.29))

        val history = dao.getHistoryForFoodItem(42).first()
        assertEquals(2, history.size)
        assertEquals(setOf("Metro", "IGA"), history.map { it.storeName }.toSet())
    }

    @Test
    fun `keeps price observations of different food items independent`() = runBlocking {
        val dao = FakePriceHistoryDao()
        val useCase = SavePriceHistoryUseCase(PriceRepositoryImpl(dao))

        useCase(priceHistory(foodItemId = 1, storeName = "Metro", price = 2.0))
        useCase(priceHistory(foodItemId = 2, storeName = "IGA", price = 5.0))

        val latestForItem1 = PriceRepositoryImpl(dao).getLatestPrice(1)
        val latestForItem2 = PriceRepositoryImpl(dao).getLatestPrice(2)

        requireNotNull(latestForItem1)
        requireNotNull(latestForItem2)
        assertEquals(2.0, latestForItem1.price, 0.0001)
        assertEquals(5.0, latestForItem2.price, 0.0001)
    }
}
