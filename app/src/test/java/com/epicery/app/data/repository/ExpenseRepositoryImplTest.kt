package com.epicery.app.data.repository

import com.epicery.app.data.local.ExpenseDao
import com.epicery.app.data.local.ExpenseEntity
import com.epicery.app.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifica que "Terminar compra" registre solo lo comprado, con el monto pagado, y devuelva cuantos items registro. */
class ExpenseRepositoryImplTest {

    private class FakeExpenseDao : ExpenseDao() {
        val rows = MutableStateFlow<List<ExpenseEntity>>(emptyList())
        var resetIds: List<Long> = emptyList()
        var archiveCalls = 0

        override fun observeAll(): Flow<List<ExpenseEntity>> = rows

        override suspend fun insertAll(expenses: List<ExpenseEntity>) {
            rows.value = rows.value + expenses.mapIndexed { i, e -> e.copy(id = rows.value.size + i + 1L) }
        }

        override suspend fun resetGroceryItems(ids: List<Long>) {
            resetIds = ids
        }

        override suspend fun archive(expenses: List<ExpenseEntity>, groceryItemIds: List<Long>) {
            archiveCalls++
            insertAll(expenses)
            resetGroceryItems(groceryItemIds)
        }
    }

    private fun item(id: Long, name: String, price: Double, purchased: Boolean, at: Long? = null, paid: Double? = null) =
        GroceryItem(id, name, "FRUITS", price, isPurchased = purchased, purchasedAt = at, pricePaid = paid)

    @Test
    fun `records only the purchased items and resets exactly those in the list`() = runBlocking {
        val dao = FakeExpenseDao()
        val repository = ExpenseRepositoryImpl(dao)
        val items = listOf(
            item(1, "Manzanas", 5.0, purchased = true, at = 1_000L),
            item(2, "Pollo", 12.0, purchased = false),
            item(3, "Leche", 4.0, purchased = true, at = 2_000L)
        )

        val archived = repository.archivePurchased(items, nowMillis = 9_999L)

        assertEquals(2, archived)
        assertEquals(listOf(1L, 3L), dao.resetIds)
        assertEquals(listOf("Manzanas", "Leche"), repository.observeExpenses().first().map { it.name })
    }

    @Test
    fun `stores the price paid when there is one and the estimate otherwise`() = runBlocking {
        val repository = ExpenseRepositoryImpl(FakeExpenseDao())

        repository.archivePurchased(
            listOf(
                item(1, "Con precio real", 5.0, purchased = true, at = 1L, paid = 4.25),
                item(2, "Sin precio real", 6.0, purchased = true, at = 1L)
            ),
            nowMillis = 9L
        )

        val amounts = repository.observeExpenses().first().associate { it.name to it.amount }
        assertEquals(4.25, amounts.getValue("Con precio real"), 0.0001)
        assertEquals(6.0, amounts.getValue("Sin precio real"), 0.0001)
    }

    @Test
    fun `keeps the purchase date and uses now for items marked without one`() = runBlocking {
        val repository = ExpenseRepositoryImpl(FakeExpenseDao())

        repository.archivePurchased(
            listOf(
                item(1, "Con fecha", 5.0, purchased = true, at = 1_234L),
                item(2, "Sin fecha", 6.0, purchased = true, at = null)
            ),
            nowMillis = 9_999L
        )

        val dates = repository.observeExpenses().first().associate { it.name to it.purchasedAt }
        assertEquals(1_234L, dates.getValue("Con fecha"))
        assertEquals(9_999L, dates.getValue("Sin fecha"))
    }

    @Test
    fun `does nothing when no item is purchased`() = runBlocking {
        val dao = FakeExpenseDao()
        val repository = ExpenseRepositoryImpl(dao)

        val archived = repository.archivePurchased(listOf(item(1, "Pollo", 12.0, purchased = false)), nowMillis = 1L)

        assertEquals(0, archived)
        assertEquals(0, dao.archiveCalls)
        assertTrue(repository.observeExpenses().first().isEmpty())
    }
}
