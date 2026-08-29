package com.epicery.app.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifica que [MIGRATION_1_2] evolucione el esquema (version 1 -> 2) sin
 * perder los datos que el usuario ya tenia guardados en `grocery_items`, y
 * que las tablas nuevas queden operativas despues de migrar.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @Test
    fun migrate1To2_preservesExistingDataAndAddsNewTables() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)

        // Simula una base de datos version 1 real (solo grocery_items), con
        // un dato que ya guardo el usuario antes de la migracion.
        val v1 = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(dbName), null)
        v1.execSQL(
            "CREATE TABLE IF NOT EXISTS `grocery_items` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`foodGroup` TEXT NOT NULL, " +
                "`estimatedPrice` REAL NOT NULL)"
        )
        v1.execSQL(
            "INSERT INTO grocery_items (name, foodGroup, estimatedPrice) VALUES ('Manzana', 'FRUITS', 1.5)"
        )
        v1.version = 1
        v1.close()

        try {
            val database = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .build()

            // El dato preexistente de grocery_items debe seguir intacto.
            val groceryItems = database.groceryItemDao().getAll().first()
            assertEquals(1, groceryItems.size)
            assertEquals("Manzana", groceryItems.first().name)
            assertEquals(1.5, groceryItems.first().estimatedPrice, 0.0001)

            // Las tablas nuevas de la version 2 deben quedar utilizables.
            val foodItemId = database.foodItemDao().insert(
                FoodItemEntity(name = "Banana", foodGroup = FoodGroup.FRUITS, category = "Frutas")
            )
            assertTrue(foodItemId > 0)

            database.close()
        } finally {
            context.deleteDatabase(dbName)
        }
    }
}
