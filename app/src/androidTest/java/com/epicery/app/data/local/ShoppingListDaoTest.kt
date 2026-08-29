package com.epicery.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingListDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ShoppingListDao
    private var foodItemId: Long = 0

    @Before
    fun createDatabase() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.shoppingListDao()
        foodItemId = database.foodItemDao().insert(
            FoodItemEntity(name = "Manzana", foodGroup = FoodGroup.FRUITS, category = "Frutas frescas")
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndGetListById_returnsInsertedList() = runTest {
        val id = dao.insertList(ShoppingListEntity(name = "Compra semanal", createdAt = 1_000L))

        val loaded = dao.getListById(id)

        assertEquals("Compra semanal", loaded?.name)
    }

    @Test
    fun getAllLists_returnsInsertedLists() = runTest {
        dao.insertList(ShoppingListEntity(name = "Compra semanal", createdAt = 1_000L))
        dao.insertList(ShoppingListEntity(name = "Compra mensual", createdAt = 2_000L))

        val lists = dao.getAllLists().first()

        assertEquals(2, lists.size)
    }

    @Test
    fun insertAndGetItemsForList_returnsInsertedItem() = runTest {
        val listId = dao.insertList(ShoppingListEntity(name = "Compra semanal", createdAt = 1_000L))

        dao.insertItem(
            ShoppingListItemEntity(shoppingListId = listId, foodItemId = foodItemId, quantity = 3.0)
        )

        val items = dao.getItemsForList(listId).first()

        assertEquals(1, items.size)
        assertEquals(3.0, items.first().quantity)
    }

    @Test
    fun updateItem_marksItAsPurchased() = runTest {
        val listId = dao.insertList(ShoppingListEntity(name = "Compra semanal", createdAt = 1_000L))
        val itemId = dao.insertItem(
            ShoppingListItemEntity(shoppingListId = listId, foodItemId = foodItemId)
        )
        val stored = dao.getItemsForList(listId).first().first { it.id == itemId }

        dao.updateItem(stored.copy(isPurchased = true))

        val updated = dao.getItemsForList(listId).first().first { it.id == itemId }
        assertTrue(updated.isPurchased)
    }

    @Test
    fun deleteList_cascadesToItsItems() = runTest {
        val listId = dao.insertList(ShoppingListEntity(name = "Compra semanal", createdAt = 1_000L))
        dao.insertItem(ShoppingListItemEntity(shoppingListId = listId, foodItemId = foodItemId))
        val stored = dao.getListById(listId)!!

        dao.deleteList(stored)

        assertNull(dao.getListById(listId))
        assertTrue(dao.getItemsForList(listId).first().isEmpty())
    }
}
