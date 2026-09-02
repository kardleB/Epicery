package com.epicery.app.data.repository

import com.epicery.app.data.local.GroceryItemDao
import com.epicery.app.data.local.GroceryItemEntity
import com.epicery.app.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica que [GroceryRepositoryImpl] (alta y edicion de items de la lista de compras, RF/CA1)
 * mapee correctamente entre [GroceryItem] de dominio y [GroceryItemEntity], delegando en
 * [GroceryItemDao] (aqui reemplazado por un fake en memoria para correr como test de JVM).
 */
class GroceryRepositoryImplTest {

    private class FakeGroceryItemDao : GroceryItemDao {
        private var nextId = 1L
        private val itemsFlow = MutableStateFlow(emptyList<GroceryItemEntity>())

        override fun getAll(): Flow<List<GroceryItemEntity>> = itemsFlow

        override suspend fun insert(item: GroceryItemEntity) {
            val withId = if (item.id == 0L) item.copy(id = nextId++) else item
            itemsFlow.value = itemsFlow.value + withId
        }

        override suspend fun update(item: GroceryItemEntity) {
            itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
        }
    }

    @Test
    fun `adds a new item to an empty shopping list`() = runBlocking {
        val repository = GroceryRepositoryImpl(FakeGroceryItemDao())

        repository.addGroceryItem(GroceryItem(name = "Manzana", foodGroup = "FRUITS", estimatedPrice = 3.50))

        val items = repository.getGroceryItems().first()
        assertEquals(1, items.size)
        assertEquals("Manzana", items.single().name)
        assertEquals(3.50, items.single().estimatedPrice, 0.0001)
        assertTrue(!items.single().isPurchased)
    }

    @Test
    fun `accumulates items added one after another`() = runBlocking {
        val repository = GroceryRepositoryImpl(FakeGroceryItemDao())

        repository.addGroceryItem(GroceryItem(name = "Manzana", foodGroup = "FRUITS", estimatedPrice = 3.50))
        repository.addGroceryItem(GroceryItem(name = "Leche", foodGroup = "DAIRY", estimatedPrice = 4.25))

        val items = repository.getGroceryItems().first()
        assertEquals(setOf("Manzana", "Leche"), items.map { it.name }.toSet())
    }

    @Test
    fun `marks an existing item as purchased without affecting the others`() = runBlocking {
        val dao = FakeGroceryItemDao()
        val repository = GroceryRepositoryImpl(dao)
        repository.addGroceryItem(GroceryItem(name = "Manzana", foodGroup = "FRUITS", estimatedPrice = 3.50))
        repository.addGroceryItem(GroceryItem(name = "Leche", foodGroup = "DAIRY", estimatedPrice = 4.25))
        val apple = repository.getGroceryItems().first().first { it.name == "Manzana" }

        repository.updateGroceryItem(apple.copy(isPurchased = true))

        val items = repository.getGroceryItems().first()
        assertTrue(items.single { it.name == "Manzana" }.isPurchased)
        assertTrue(!items.single { it.name == "Leche" }.isPurchased)
    }

    @Test
    fun `updates the estimated price of an existing item`() = runBlocking {
        val dao = FakeGroceryItemDao()
        val repository = GroceryRepositoryImpl(dao)
        repository.addGroceryItem(GroceryItem(name = "Pollo", foodGroup = "PROTEIN", estimatedPrice = 10.0))
        val chicken = repository.getGroceryItems().first().single()

        repository.updateGroceryItem(chicken.copy(estimatedPrice = 12.50))

        val updated = repository.getGroceryItems().first().single()
        assertEquals(12.50, updated.estimatedPrice, 0.0001)
    }

    @Test
    fun `returns an empty list when no items were added`() = runBlocking {
        val repository = GroceryRepositoryImpl(FakeGroceryItemDao())

        val items = repository.getGroceryItems().first()

        assertTrue(items.isEmpty())
    }
}
