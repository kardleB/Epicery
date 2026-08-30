package com.epicery.app.di

import com.epicery.app.data.repository.BudgetRepositoryImpl
import com.epicery.app.data.repository.FoodRepositoryImpl
import com.epicery.app.data.repository.GroceryRepositoryImpl
import com.epicery.app.data.repository.PriceRepositoryImpl
import com.epicery.app.data.repository.UsdaFoodDataRepositoryImpl
import com.epicery.app.domain.repository.BudgetRepository
import com.epicery.app.domain.repository.FoodRepository
import com.epicery.app.domain.repository.GroceryRepository
import com.epicery.app.domain.repository.PriceRepository
import com.epicery.app.domain.repository.UsdaFoodDataRepository
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

    @Binds
    @Singleton
    abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository

    @Binds
    @Singleton
    abstract fun bindPriceRepository(impl: PriceRepositoryImpl): PriceRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindUsdaFoodDataRepository(impl: UsdaFoodDataRepositoryImpl): UsdaFoodDataRepository
}
