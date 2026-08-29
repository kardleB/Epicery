package com.epicery.app.di

import android.content.Context
import androidx.room.Room
import com.epicery.app.data.local.AppDatabase
import com.epicery.app.data.local.GroceryItemDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME).build()

    @Provides
    fun provideGroceryItemDao(database: AppDatabase): GroceryItemDao = database.groceryItemDao()
}
