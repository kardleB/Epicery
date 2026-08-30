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
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica que [GetFoodItemsByCategoryUseCase] consume correctamente a
 * [FoodRepositoryImpl], que a su vez encapsula el acceso al [FoodItemDao] de
 * Room (aqui reemplazado por un fake en memoria para poder correr como test
 * de JVM, sin depender de una base de datos instrumentada).
 */
class GetFoodItemsByCategoryUseCaseTest {

    private class FakeFoodItemDao(seed: List<FoodItemEntity>) : FoodItemDao {
        private val itemsFlow = MutableStateFlow(seed)

        override suspend fun insert(foodItem: FoodItemEntity): Long {
            itemsFlow.value = itemsFlow.value + foodItem
            return foodItem.id
        }

        override suspend fun insertAll(foodItems: List<FoodItemEntity>): List<Long> {
            itemsFlow.value = itemsFlow.value + foodItems
            return foodItems.map { it.id }
        }

        override suspend fun update(foodItem: FoodItemEntity) {
            itemsFlow.value = itemsFlow.value.map { if (it.id == foodItem.id) foodItem else it }
        }

        override suspend fun delete(foodItem: FoodItemEntity) {
            itemsFlow.value = itemsFlow.value.filterNot { it.id == foodItem.id }
        }

        override suspend fun getById(id: Long): FoodItemEntity? =
            itemsFlow.value.find { it.id == id }

        override fun getAll(): Flow<List<FoodItemEntity>> = itemsFlow

        override fun getByFoodGroup(foodGroup: FoodGroup): Flow<List<FoodItemEntity>> =
            itemsFlow.map { items -> items.filter { it.foodGroup == foodGroup } }

        override fun getByCategory(category: String): Flow<List<FoodItemEntity>> =
            itemsFlow.map { items -> items.filter { it.category == category } }
    }

    @Test
    fun `returns only food items matching the requested category`() = runBlocking {
        val apple = FoodItemEntity(id = 1, name = "Manzana", foodGroup = FoodGroup.FRUITS, category = "fresh")
        val banana = FoodItemEntity(id = 2, name = "Banana", foodGroup = FoodGroup.FRUITS, category = "fresh")
        val rice = FoodItemEntity(id = 3, name = "Arroz", foodGroup = FoodGroup.GRAINS, category = "pantry")
        val dao = FakeFoodItemDao(seed = listOf(apple, banana, rice))
        val useCase = GetFoodItemsByCategoryUseCase(FoodRepositoryImpl(dao))

        val result = useCase("fresh").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "fresh" })
        assertEquals(setOf("Manzana", "Banana"), result.map { it.name }.toSet())
    }
}
