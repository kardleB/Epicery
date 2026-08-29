package com.epicery.app.di

import com.epicery.app.data.repository.GroceryRepositoryImpl
import com.epicery.app.domain.repository.GroceryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGroceryRepository(impl: GroceryRepositoryImpl): GroceryRepository
}
