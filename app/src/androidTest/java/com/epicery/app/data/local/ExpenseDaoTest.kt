package com.epicery.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica con Room real que [ExpenseDao.archive] pase los items a gastos y los quite de la lista
 * en una sola transaccion, sin tocar los items que no se archivan.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun archive_movesPurchasedItemsToExpensesAndKeepsTheRest() = runTest {
        val groceryDao = database.groceryItemDao()
        groceryDao.insert(GroceryItemEntity(name = "Manzanas", foodGroup = "FRUITS", estimatedPrice = 5.0, isPurchased = true, purchasedAt = 1_000L))
        groceryDao.insert(GroceryItemEntity(name = "Pollo", foodGroup = "PROTEIN", estimatedPrice = 12.0))
        val items = groceryDao.getAll().first()
        val purchased = items.single { it.name == "Manzanas" }

        database.expenseDao().archive(
            expenses = listOf(ExpenseEntity(name = purchased.name, foodGroup = purchased.foodGroup, amount = 4.5, purchasedAt = 1_000L)),
            groceryItemIds = listOf(purchased.id)
        )

        assertEquals(listOf("Pollo"), groceryDao.getAll().first().map { it.name })
        val expenses = database.expenseDao().observeAll().first()
        assertEquals(1, expenses.size)
        assertEquals("Manzanas", expenses.single().name)
        assertEquals(4.5, expenses.single().amount, 0.0001)
    }

    @Test
    fun archive_listsTheMostRecentPurchaseFirst() = runTest {
        database.expenseDao().insertAll(
            listOf(
                ExpenseEntity(name = "Vieja", foodGroup = "DAIRY", amount = 1.0, purchasedAt = 1_000L),
                ExpenseEntity(name = "Nueva", foodGroup = "DAIRY", amount = 2.0, purchasedAt = 2_000L)
            )
        )

        assertEquals(listOf("Nueva", "Vieja"), database.expenseDao().observeAll().first().map { it.name })
        assertTrue(database.groceryItemDao().getAll().first().isEmpty())
    }
}
