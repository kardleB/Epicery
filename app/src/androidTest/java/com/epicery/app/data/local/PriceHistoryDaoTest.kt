package com.epicery.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PriceHistoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: PriceHistoryDao
    private var foodItemId: Long = 0

    @Before
    fun createDatabase() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.priceHistoryDao()
        foodItemId = database.foodItemDao().insert(
            FoodItemEntity(name = "Manzana", foodGroup = FoodGroup.FRUITS, category = "Frutas frescas")
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndGetById_returnsInsertedPriceHistory() = runTest {
        val id = dao.insert(
            PriceHistoryEntity(foodItemId = foodItemId, storeName = "Metro", price = 1.5, recordedAt = 1_000L)
        )

        val loaded = dao.getById(id)

        assertEquals("Metro", loaded?.storeName)
        assertEquals(1.5, loaded?.price)
    }

    @Test
    fun getHistoryForFoodItem_isOrderedByMostRecentFirst() = runTest {
        dao.insert(PriceHistoryEntity(foodItemId = foodItemId, storeName = "Metro", price = 1.5, recordedAt = 1_000L))
        dao.insert(PriceHistoryEntity(foodItemId = foodItemId, storeName = "Metro", price = 1.8, recordedAt = 2_000L))

        val history = dao.getHistoryForFoodItem(foodItemId).first()

        assertEquals(2, history.size)
        assertEquals(1.8, history.first().price)
    }

    @Test
    fun getLatestPriceForFoodItem_returnsMostRecentEntry() = runTest {
        dao.insert(PriceHistoryEntity(foodItemId = foodItemId, storeName = "Metro", price = 1.5, recordedAt = 1_000L))
        dao.insert(PriceHistoryEntity(foodItemId = foodItemId, storeName = "IGA", price = 1.9, recordedAt = 3_000L))

        val latest = dao.getLatestPriceForFoodItem(foodItemId)

        assertEquals("IGA", latest?.storeName)
    }

    @Test
    fun delete_removesPriceHistory() = runTest {
        val id = dao.insert(
            PriceHistoryEntity(foodItemId = foodItemId, storeName = "Metro", price = 1.5, recordedAt = 1_000L)
        )
        val stored = dao.getById(id)!!

        dao.delete(stored)

        assertNull(dao.getById(id))
    }
}
