package com.epicery.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migracion de la version 1 (solo [GroceryItemEntity]) a la version 2, que
 * agrega el catalogo de alimentos, el historico de precios y las listas de
 * compras. Se define de forma explicita (en vez de recrear la base) para no
 * perder los datos que el usuario ya tenga guardados en `grocery_items`.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `food_items` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`foodGroup` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`servingSizeGrams` REAL NOT NULL DEFAULT 1, " +
                "`calories` REAL NOT NULL DEFAULT 0, " +
                "`proteinGrams` REAL NOT NULL DEFAULT 0, " +
                "`carbsGrams` REAL NOT NULL DEFAULT 0, " +
                "`fatGrams` REAL NOT NULL DEFAULT 0, " +
                "`fiberGrams` REAL NOT NULL DEFAULT 0, " +
                "`sodiumMg` REAL NOT NULL DEFAULT 0, " +
                "`addedSugarGrams` REAL NOT NULL DEFAULT 0, " +
                "`isWholeGrain` INTEGER NOT NULL DEFAULT 0, " +
                "`isProcessed` INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_food_items_foodGroup` ON `food_items` (`foodGroup`)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `price_history` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`foodItemId` INTEGER NOT NULL, " +
                "`storeName` TEXT NOT NULL, " +
                "`price` REAL NOT NULL, " +
                "`currency` TEXT NOT NULL DEFAULT 'CAD', " +
                "`recordedAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_price_history_foodItemId` ON `price_history` (`foodItemId`)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `shopping_lists` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`estimatedBudget` REAL NOT NULL DEFAULT 0, " +
                "`isCompleted` INTEGER NOT NULL DEFAULT 0)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `shopping_list_items` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`shoppingListId` INTEGER NOT NULL, " +
                "`foodItemId` INTEGER NOT NULL, " +
                "`quantity` REAL NOT NULL DEFAULT 1, " +
                "`unit` TEXT NOT NULL DEFAULT 'unidad', " +
                "`estimatedPrice` REAL NOT NULL DEFAULT 0, " +
                "`isPurchased` INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(`shoppingListId`) REFERENCES `shopping_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_shopping_list_items_shoppingListId` ON `shopping_list_items` (`shoppingListId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_shopping_list_items_foodItemId` ON `shopping_list_items` (`foodItemId`)"
        )
    }
}

/**
 * Migracion de la version 2 a la version 3, que agrega las tablas de cache
 * persistida de respuestas de USDA FoodData Central y de GroceryPulse
 * (RNF5: soporte offline-first, evita repetir llamadas de red ya resueltas).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `usda_nutrition_cache` (" +
                "`query` TEXT NOT NULL PRIMARY KEY, " +
                "`fdcId` INTEGER NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`calories` REAL NOT NULL, " +
                "`proteinGrams` REAL NOT NULL, " +
                "`sodiumMg` REAL NOT NULL, " +
                "`sugarGrams` REAL NOT NULL, " +
                "`fetchedAt` INTEGER NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `grocery_price_cache` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`query` TEXT NOT NULL, " +
                "`storeName` TEXT NOT NULL, " +
                "`productName` TEXT NOT NULL, " +
                "`price` REAL NOT NULL, " +
                "`currency` TEXT NOT NULL, " +
                "`city` TEXT NOT NULL, " +
                "`sourceUrl` TEXT, " +
                "`fetchedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_grocery_price_cache_query` ON `grocery_price_cache` (`query`)"
        )
    }
}

/**
 * Migracion de la version 3 a la version 4, que agrega el estado de compra
 * por item (RF2/CA1: marcar/desmarcar articulos como comprados en la
 * pantalla de Shopping List) sin perder los items que el usuario ya tenga
 * guardados en `grocery_items`.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `grocery_items` ADD COLUMN `isPurchased` INTEGER NOT NULL DEFAULT 0"
        )
    }
}

/**
 * Todas las migraciones conocidas de [AppDatabase], en orden. Cada vez que se
 * incremente `version` en [AppDatabase] hay que agregar aqui la migracion
 * correspondiente para que los datos existentes del usuario no se pierdan.
 */
val APP_DATABASE_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
