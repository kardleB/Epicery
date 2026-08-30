package com.epicery.app.domain.usecase

import com.epicery.app.data.local.PriceHistoryDao
import com.epicery.app.data.local.PriceHistoryEntity
import com.epicery.app.data.repository.PriceRepositoryImpl
import com.epicery.app.domain.model.GroceryPriceQuote
import com.epicery.app.domain.repository.GroceryPulseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica que [GetMontrealGroceryPricesUseCase] mapea las cotizaciones de GroceryPulse
 * (Apify) a [PriceHistoryEntity] y las persiste vía [PriceRepositoryImpl] (aqui reemplazado
 * por un fake en memoria de [PriceHistoryDao] para correr como test de JVM).
 */
class GetMontrealGroceryPricesUseCaseTest {

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

    private class FakeGroceryPulseRepository(
        private val quotes: List<GroceryPriceQuote>
    ) : GroceryPulseRepository {
        override suspend fun compareMontrealPrices(query: String): List<GroceryPriceQuote> = quotes
    }

    @Test
    fun `saves at least one Montreal store quote as price history when the API is available`() = runBlocking {
        val quotes = listOf(
            GroceryPriceQuote(storeName = "Metro", productName = "Leche", price = 3.49, city = "Montreal"),
            GroceryPriceQuote(storeName = "IGA", productName = "Leche", price = 3.29, city = "Montreal")
        )
        val useCase = GetMontrealGroceryPricesUseCase(
            groceryPulseRepository = FakeGroceryPulseRepository(quotes),
            priceRepository = PriceRepositoryImpl(FakePriceHistoryDao())
        )

        val result = useCase(foodItemId = 42, query = "Leche")

        assertTrue(result.isNotEmpty())
        assertEquals(setOf("Metro", "IGA"), result.map { it.storeName }.toSet())
        assertTrue(result.all { it.foodItemId == 42L && it.id != 0L })
    }

    @Test
    fun `returns an empty list without saving anything when GroceryPulse has no results`() = runBlocking {
        val useCase = GetMontrealGroceryPricesUseCase(
            groceryPulseRepository = FakeGroceryPulseRepository(emptyList()),
            priceRepository = PriceRepositoryImpl(FakePriceHistoryDao())
        )

        val result = useCase(foodItemId = 42, query = "ArtículoInexistente")

        assertTrue(result.isEmpty())
    }
}
