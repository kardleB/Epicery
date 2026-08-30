package com.epicery.app.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroceryPriceCacheDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: GroceryPriceCacheDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.groceryPriceCacheDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getByQuery_returnsEmptyListWhenNothingCached() = runTest {
        assertTrue(dao.getByQuery("leche").isEmpty())
    }

    @Test
    fun replaceForQuery_insertsAllQuotesForThatQuery() = runTest {
        dao.replaceForQuery(
            "leche",
            listOf(
                GroceryPriceCacheEntity(
                    query = "leche",
                    storeName = "Metro",
                    productName = "Leche",
                    price = 3.49,
                    currency = "CAD",
                    city = "Montreal",
                    sourceUrl = null,
                    fetchedAt = 1_000L
                ),
                GroceryPriceCacheEntity(
                    query = "leche",
                    storeName = "IGA",
                    productName = "Leche",
                    price = 3.29,
                    currency = "CAD",
                    city = "Montreal",
                    sourceUrl = null,
                    fetchedAt = 1_000L
                )
            )
        )

        val cached = dao.getByQuery("leche")

        assertEquals(2, cached.size)
        assertEquals(setOf("Metro", "IGA"), cached.map { it.storeName }.toSet())
    }

    @Test
    fun replaceForQuery_removesPreviousEntriesForSameQuery() = runTest {
        dao.replaceForQuery(
            "leche",
            listOf(
                GroceryPriceCacheEntity(
                    query = "leche",
                    storeName = "Metro",
                    productName = "Leche",
                    price = 3.49,
                    currency = "CAD",
                    city = "Montreal",
                    sourceUrl = null,
                    fetchedAt = 1_000L
                )
            )
        )

        dao.replaceForQuery(
            "leche",
            listOf(
                GroceryPriceCacheEntity(
                    query = "leche",
                    storeName = "IGA",
                    productName = "Leche",
                    price = 3.19,
                    currency = "CAD",
                    city = "Montreal",
                    sourceUrl = null,
                    fetchedAt = 2_000L
                )
            )
        )

        val cached = dao.getByQuery("leche")

        assertEquals(1, cached.size)
        assertEquals("IGA", cached.first().storeName)
    }

    @Test
    fun replaceForQuery_doesNotAffectOtherQueries() = runTest {
        dao.replaceForQuery(
            "leche",
            listOf(
                GroceryPriceCacheEntity(
                    query = "leche",
                    storeName = "Metro",
                    productName = "Leche",
                    price = 3.49,
                    currency = "CAD",
                    city = "Montreal",
                    sourceUrl = null,
                    fetchedAt = 1_000L
                )
            )
        )
        dao.replaceForQuery("pan", emptyList())

        val cached = dao.getByQuery("leche")

        assertEquals(1, cached.size)
    }
}
