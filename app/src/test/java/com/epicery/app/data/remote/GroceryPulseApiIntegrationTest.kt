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
 * Pruebas de integración de [GroceryPulseApi]: arman el mismo stack Retrofit + OkHttp +
 * [RetryInterceptor] que usa la app en producción (ver `NetworkModule`), pero apuntando a un
 * [MockWebServer] local en vez de `api.apify.com`, para no depender de la red ni de una cuenta
 * de Apify real en CI. Cubren tanto el mapeo de una respuesta exitosa como los escenarios de
 * error que `GroceryPulseRepositoryImpl` necesita distinguir (timeout, dataset vacío, y
 * rate limiting vía HTTP 429).
 */
class GroceryPulseApiIntegrationTest {

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
    ): GroceryPulseApi {
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
        return retrofit.create(GroceryPulseApi::class.java)
    }

    @Test
    fun `maps a successful dataset response into the expected price quotes`() = runBlocking {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                [
                  {"store": "Metro", "title": "Leche 2%", "price": 3.49, "currency": "CAD", "city": "Montreal", "url": "https://example.com/metro"},
                  {"store": "IGA", "title": "Leche 2%", "price": 3.29, "currency": "CAD", "city": "Montreal", "url": "https://example.com/iga"}
                ]
                """.trimIndent()
            )
        )

        val result = apiWith().compareGroceryPrices(
            actorId = "test-actor",
            token = "test-token",
            request = GroceryPulseRequest(query = "leche")
        )

        assertEquals(2, result.size)
        assertEquals("Metro", result[0].store)
        assertEquals(3.49, result[0].price, 0.0)
        assertEquals("IGA", result[1].store)

        val request = server.takeRequest()
        assertTrue(request.path.orEmpty().contains("/acts/test-actor/run-sync-get-dataset-items"))
        assertTrue(request.path.orEmpty().contains("token=test-token"))
        assertTrue(request.body.readUtf8().contains("\"leche\""))
    }

    @Test
    fun `maps an empty dataset without throwing`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val result = apiWith().compareGroceryPrices(
            actorId = "test-actor",
            token = "test-token",
            request = GroceryPulseRequest(query = "artículo-inexistente")
        )

        assertTrue(result.isEmpty())
    }

    @Test(expected = HttpException::class)
    fun `throws after exhausting retries when the rate limit keeps being exceeded`(): Unit = runBlocking {
        repeat(2) { server.enqueue(MockResponse().setResponseCode(429)) }

        try {
            apiWith(maxRetries = 1, baseDelayMs = 10).compareGroceryPrices(
                actorId = "test-actor",
                token = "test-token",
                request = GroceryPulseRequest(query = "leche")
            )
        } catch (e: HttpException) {
            assertEquals(429, e.code())
            assertEquals(2, server.requestCount)
            throw e
        }
    }

    @Test
    fun `recovers from a single rate limit response by retrying`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val result = apiWith(maxRetries = 1, baseDelayMs = 10).compareGroceryPrices(
            actorId = "test-actor",
            token = "test-token",
            request = GroceryPulseRequest(query = "leche")
        )

        assertTrue(result.isEmpty())
        assertEquals(2, server.requestCount)
    }

    @Test(expected = SocketTimeoutException::class)
    fun `throws a timeout when the server takes too long to respond`(): Unit = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )

        apiWith(maxRetries = 0, timeoutMs = 200).compareGroceryPrices(
            actorId = "test-actor",
            token = "test-token",
            request = GroceryPulseRequest(query = "leche")
        )
    }
}
