package com.epicery.app.di

import android.content.Context
import androidx.room.Room
import com.epicery.app.BuildConfig
import com.epicery.app.data.local.APP_DATABASE_MIGRATIONS
import com.epicery.app.data.local.AppDatabase
import com.epicery.app.data.local.FoodItemDao
import com.epicery.app.data.local.GroceryItemDao
import com.epicery.app.data.local.GroceryPriceCacheDao
import com.epicery.app.data.local.PriceHistoryDao
import com.epicery.app.data.local.ShoppingListDao
import com.epicery.app.data.local.UsdaNutritionCacheDao
import com.epicery.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .addMigrations(*APP_DATABASE_MIGRATIONS)

        // La recreacion destructiva del esquema solo se permite en builds de
        // debug, como red de seguridad mientras se itera el modelo de datos.
        // En produccion toda evolucion del esquema debe llegar via una
        // Migration explicita en APP_DATABASE_MIGRATIONS para no perder los
        // datos del usuario.
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }

        return builder.build()
    }

    @Provides
    fun provideGroceryItemDao(database: AppDatabase): GroceryItemDao = database.groceryItemDao()

    @Provides
    fun provideFoodItemDao(database: AppDatabase): FoodItemDao = database.foodItemDao()

    @Provides
    fun providePriceHistoryDao(database: AppDatabase): PriceHistoryDao = database.priceHistoryDao()

    @Provides
    fun provideShoppingListDao(database: AppDatabase): ShoppingListDao = database.shoppingListDao()

    @Provides
    fun provideUsdaNutritionCacheDao(database: AppDatabase): UsdaNutritionCacheDao =
        database.usdaNutritionCacheDao()

    @Provides
    fun provideGroceryPriceCacheDao(database: AppDatabase): GroceryPriceCacheDao =
        database.groceryPriceCacheDao()
}
