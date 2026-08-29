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
class FoodItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FoodItemDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.foodItemDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun sampleFoodItem(
        name: String = "Manzana",
        foodGroup: FoodGroup = FoodGroup.FRUITS,
        category: String = "Frutas frescas"
    ) = FoodItemEntity(name = name, foodGroup = foodGroup, category = category)

    @Test
    fun insertAndGetById_returnsInsertedFoodItem() = runTest {
        val id = dao.insert(sampleFoodItem())

        val loaded = dao.getById(id)

        assertEquals("Manzana", loaded?.name)
        assertEquals(FoodGroup.FRUITS, loaded?.foodGroup)
    }

    @Test
    fun getByFoodGroup_returnsOnlyMatchingItems() = runTest {
        dao.insert(sampleFoodItem(name = "Manzana", foodGroup = FoodGroup.FRUITS))
        dao.insert(sampleFoodItem(name = "Zanahoria", foodGroup = FoodGroup.VEGETABLES, category = "Vegetales"))

        val fruits = dao.getByFoodGroup(FoodGroup.FRUITS).first()

        assertEquals(1, fruits.size)
        assertEquals("Manzana", fruits.first().name)
    }

    @Test
    fun getByCategory_returnsOnlyMatchingItems() = runTest {
        dao.insert(sampleFoodItem(name = "Manzana", category = "Frutas frescas"))
        dao.insert(sampleFoodItem(name = "Banana", category = "Frutas tropicales"))

        val result = dao.getByCategory("Frutas tropicales").first()

        assertEquals(1, result.size)
        assertEquals("Banana", result.first().name)
    }

    @Test
    fun update_modifiesStoredFoodItem() = runTest {
        val id = dao.insert(sampleFoodItem())
        val updated = dao.getById(id)!!.copy(calories = 95.0)

        dao.update(updated)

        assertEquals(95.0, dao.getById(id)?.calories)
    }

    @Test
    fun delete_removesFoodItem() = runTest {
        val id = dao.insert(sampleFoodItem())
        val stored = dao.getById(id)!!

        dao.delete(stored)

        assertNull(dao.getById(id))
    }
}
