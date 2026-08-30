package com.epicery.app.di

import com.epicery.app.data.remote.EpiceryApiService
import com.epicery.app.data.remote.GroceryPulseApi
import com.epicery.app.data.remote.RetryInterceptor
import com.epicery.app.data.remote.UsdaFoodDataApi
import com.epicery.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(RetryInterceptor())
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideEpiceryApiService(retrofit: Retrofit): EpiceryApiService =
        retrofit.create(EpiceryApiService::class.java)

    @Provides
    @Singleton
    @Named("usda")
    fun provideUsdaRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.USDA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideUsdaFoodDataApi(@Named("usda") retrofit: Retrofit): UsdaFoodDataApi =
        retrofit.create(UsdaFoodDataApi::class.java)

    @Provides
    @Singleton
    @Named("apify")
    fun provideApifyRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.APIFY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGroceryPulseApi(@Named("apify") retrofit: Retrofit): GroceryPulseApi =
        retrofit.create(GroceryPulseApi::class.java)
}
