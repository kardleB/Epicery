package com.epicery.app.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface EpiceryApiService {
    @GET("health")
    suspend fun health(): Response<Unit>
}
