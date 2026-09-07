package com.epicery.app.domain.usecase

import com.epicery.app.data.local.FoodGroup
import com.epicery.app.data.local.FoodItemDao
import com.epicery.app.data.local.FoodItemEntity
import com.epicery.app.data.repository.FoodRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifica que [SyncFoodItemToCatalogUseCase] da de alta un [com.epicery.app.domain.model.FoodItem]
 * la primera vez que ve un nombre, y no crea duplicados en altas siguientes del mismo nombre
 * (comparación case-insensitive, igual que hace `GroceryItem` -> `FoodItem` desde
 * `ShoppingListViewModel.addItem`).
 */
class SyncFoodItemToCatalogUseCaseTest {

    private class FakeFoodItemDao(seed: List<FoodItemEntity> = emptyList()) : FoodItemDao {
        private var nextId = 1L
        private val itemsFlow = MutableStateFlow(seed)

        override suspend fun insert(foodItem: FoodItemEntity): Long {
            val withId = if (foodItem.id == 0L) foodItem.copy(id = nextId++) else foodItem
            itemsFlow.value = itemsFlow.value + withId
            return withId.id
        }

        override suspend fun insertAll(foodItems: List<FoodItemEntity>): List<Long> =
            foodItems.map { insert(it) }

        override suspend fun update(foodItem: FoodItemEntity) {
            itemsFlow.value = itemsFlow.value.map { if (it.id == foodItem.id) foodItem else it }
        }

        override suspend fun delete(foodItem: FoodItemEntity) {
            itemsFlow.value = itemsFlow.value.filterNot { it.id == foodItem.id }
        }

        override suspend fun getById(id: Long): FoodItemEntity? =
            itemsFlow.value.find { it.id == id }

        override suspend fun getByName(name: String): FoodItemEntity? =
            itemsFlow.value.find { it.name.equals(name, ignoreCase = true) }

        override fun getAll(): Flow<List<FoodItemEntity>> = itemsFlow

        override fun getByFoodGroup(foodGroup: FoodGroup): Flow<List<FoodItemEntity>> =
            itemsFlow.map { items -> items.filter { it.foodGroup == foodGroup } }

        override fun getByCategory(category: String): Flow<List<FoodItemEntity>> =
            itemsFlow.map { items -> items.filter { it.category == category } }
    }

    @Test
    fun `creates a new catalog entry the first time it sees a name`() = runBlocking {
        val dao = FakeFoodItemDao()
        val useCase = SyncFoodItemToCatalogUseCase(FoodRepositoryImpl(dao))

        useCase("Manzanas", FoodGroup.FRUITS)

        val created = dao.getByName("Manzanas")
        assertEquals("Manzanas", created?.name)
        assertEquals(FoodGroup.FRUITS, created?.foodGroup)
    }

    @Test
    fun `does not create a duplicate when the name already exists, case-insensitively`() = runBlocking {
        val existing = FoodItemEntity(id = 1, name = "Leche", foodGroup = FoodGroup.DAIRY, category = "DAIRY")
        val dao = FakeFoodItemDao(seed = listOf(existing))
        val useCase = SyncFoodItemToCatalogUseCase(FoodRepositoryImpl(dao))

        useCase("leche", FoodGroup.DAIRY)

        assertEquals(1, dao.getAll().first().size)
    }
}
