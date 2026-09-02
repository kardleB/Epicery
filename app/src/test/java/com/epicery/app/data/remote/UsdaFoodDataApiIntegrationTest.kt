package com.epicery.app.data.remote

import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Pruebas de integración de [UsdaFoodDataApi]: arman el mismo stack Retrofit + OkHttp +
 * [RetryInterceptor] que usa la app en producción (ver `NetworkModule`), pero apuntando a un
 * [MockWebServer] local en vez de `api.nal.usda.gov`, para no depender de la red ni de una
 * API key real en CI. Cubren tanto el mapeo de una respuesta exitosa como los escenarios de
 * error que `UsdaFoodDataRepositoryImpl` necesita distinguir (timeout, respuesta vacía, y
 * el límite de 1000 req/hora de USDA vía HTTP 429).
 */
class UsdaFoodDataApiIntegrationTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun apiWith(
        maxRetries: Int = 2,
        baseDelayMs: Long = 10,
        timeoutMs: Long = 2000
    ): UsdaFoodDataApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .addInterceptor(RetryInterceptor(maxRetries = maxRetries, baseDelayMs = baseDelayMs))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(UsdaFoodDataApi::class.java)
    }

    @Test
    fun `maps a successful search response into the expected food and nutrients`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "totalHits": 1,
                  "foods": [
                    {
                      "fdcId": 1102702,
                      "description": "Apple, raw",
                      "foodNutrients": [
                        {"nutrientName": "Energy", "nutrientNumber": "208", "unitName": "KCAL", "value": 52.0},
                        {"nutrientName": "Protein", "nutrientNumber": "203", "unitName": "G", "value": 0.3}
                      ]
                    }
                  ]
                }
                """.trimIndent()
            )
        )

        val result = apiWith().searchFoods(query = "apple", apiKey = "test-key")

        assertEquals(1, result.totalHits)
        val food = result.foods?.single()
        assertEquals(1102702L, food?.fdcId)
        assertEquals("Apple, raw", food?.description)
        assertEquals(52.0, food?.foodNutrients?.first { it.nutrientName == "Energy" }?.value)

        val request = server.takeRequest()
        assertTrue(request.path.orEmpty().contains("query=apple"))
        assertTrue(request.path.orEmpty().contains("api_key=test-key"))
    }

    @Test
    fun `maps an empty result set without throwing`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"totalHits": 0, "foods": []}"""))

        val result = apiWith().searchFoods(query = "xyzzyunknown", apiKey = "test-key")

        assertEquals(0, result.totalHits)
        assertTrue(result.foods.orEmpty().isEmpty())
    }

    @Test(expected = HttpException::class)
    fun `throws after exhausting retries when the USDA rate limit keeps being exceeded`(): Unit = runBlocking {
        repeat(2) { server.enqueue(MockResponse().setResponseCode(429)) }

        try {
            apiWith(maxRetries = 1, baseDelayMs = 10).searchFoods(query = "apple", apiKey = "test-key")
        } catch (e: HttpException) {
            assertEquals(429, e.code())
            assertEquals(2, server.requestCount)
            throw e
        }
    }

    @Test
    fun `recovers from a single rate limit response by retrying`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"totalHits": 0, "foods": []}"""))

        val result = apiWith(maxRetries = 1, baseDelayMs = 10).searchFoods(query = "apple", apiKey = "test-key")

        assertEquals(0, result.totalHits)
        assertEquals(2, server.requestCount)
    }

    @Test(expected = SocketTimeoutException::class)
    fun `throws a timeout when the server takes too long to respond`(): Unit = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"totalHits": 0, "foods": []}""")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )

        apiWith(maxRetries = 0, timeoutMs = 200).searchFoods(query = "apple", apiKey = "test-key")
    }
}
