package com.epicery.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsdaNutritionCacheDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: UsdaNutritionCacheDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.usdaNutritionCacheDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getByQuery_returnsNullWhenNothingCached() = runTest {
        assertNull(dao.getByQuery("manzana"))
    }

    @Test
    fun upsert_thenGetByQuery_returnsCachedEntry() = runTest {
        dao.upsert(
            UsdaNutritionCacheEntity(
                query = "manzana",
                fdcId = 123L,
                description = "Apple, raw",
                calories = 52.0,
                proteinGrams = 0.3,
                sodiumMg = 1.0,
                sugarGrams = 10.0,
                fetchedAt = 1_000L
            )
        )

        val cached = dao.getByQuery("manzana")

        assertEquals("Apple, raw", cached?.description)
        assertEquals(52.0, cached?.calories)
    }

    @Test
    fun upsert_withSameQuery_replacesPreviousEntry() = runTest {
        dao.upsert(
            UsdaNutritionCacheEntity(
                query = "manzana",
                fdcId = 123L,
                description = "Apple, raw",
                calories = 52.0,
                proteinGrams = 0.3,
                sodiumMg = 1.0,
                sugarGrams = 10.0,
                fetchedAt = 1_000L
            )
        )
        dao.upsert(
            UsdaNutritionCacheEntity(
                query = "manzana",
                fdcId = 456L,
                description = "Apple, updated",
                calories = 60.0,
                proteinGrams = 0.4,
                sodiumMg = 2.0,
                sugarGrams = 12.0,
                fetchedAt = 2_000L
            )
        )

        val cached = dao.getByQuery("manzana")

        assertEquals("Apple, updated", cached?.description)
        assertEquals(2_000L, cached?.fetchedAt)
    }
}
