package com.epicery.app.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica que la cadena de migraciones hasta la version 5 (incluida [MIGRATION_4_5], que agrega
 * `purchasedAt` y `pricePaid`) no pierda los items que el usuario ya tenia en `grocery_items`.
 */
@RunWith(AndroidJUnit4::class)
class Migration4To5Test {

    private val dbName = "migration-4-5-test.db"

    @Test
    fun migrate4To5_keepsExistingItems() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        val v1 = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(dbName), null)
        v1.execSQL(
            "CREATE TABLE IF NOT EXISTS `grocery_items` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`foodGroup` TEXT NOT NULL, " +
                "`estimatedPrice` REAL NOT NULL)"
        )
        v1.execSQL("INSERT INTO grocery_items (name, foodGroup, estimatedPrice) VALUES ('Manzana', 'FRUITS', 1.5)")
        v1.execSQL("INSERT INTO grocery_items (name, foodGroup, estimatedPrice) VALUES ('Leche', 'DAIRY', 4.0)")
        v1.version = 1  // base de la primera version de la app
        v1.close()

        try {
            val database = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                .addMigrations(*APP_DATABASE_MIGRATIONS)
                .allowMainThreadQueries()
                .build()

            val milk = database.groceryItemDao().getAll().first().first { it.name == "Leche" }
            database.groceryItemDao().update(milk.copy(isPurchased = true))

            val items = database.groceryItemDao().getAll().first().associateBy { it.name }
            assertEquals(2, items.size)
            assertEquals(1.5, items.getValue("Manzana").estimatedPrice, 0.0001)
            assertNull(items.getValue("Manzana").purchasedAt)
            assertNull(items.getValue("Manzana").pricePaid)
            assertEquals(true, items.getValue("Leche").isPurchased)

            database.close()
        } finally {
            context.deleteDatabase(dbName)
        }
    }
}
