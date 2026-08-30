package com.epicery.app.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Verifica que [toApiFailureReason] distingue correctamente el rate limiting de USDA
 * (HTTP 429) de otros fallos de red/servidor, ya que de esa clasificación depende el
 * mensaje de fallback que ven los repositorios al degradar a datos locales.
 */
class ApiFailureReasonTest {

    private fun httpExceptionWithCode(code: Int): HttpException {
        val body = "error".toResponseBody("text/plain".toMediaTypeOrNull())
        return HttpException(Response.error<Any>(code, body))
    }

    @Test
    fun `classifies HTTP 429 as rate limited`() {
        assertEquals(ApiFailureReason.RATE_LIMITED, httpExceptionWithCode(429).toApiFailureReason())
    }

    @Test
    fun `classifies HTTP 500 to 599 as server error`() {
        assertEquals(ApiFailureReason.SERVER_ERROR, httpExceptionWithCode(500).toApiFailureReason())
        assertEquals(ApiFailureReason.SERVER_ERROR, httpExceptionWithCode(503).toApiFailureReason())
    }

    @Test
    fun `classifies other HTTP error codes as client error`() {
        assertEquals(ApiFailureReason.CLIENT_ERROR, httpExceptionWithCode(404).toApiFailureReason())
    }

    @Test
    fun `classifies socket timeout as timeout`() {
        assertEquals(ApiFailureReason.TIMEOUT, SocketTimeoutException().toApiFailureReason())
    }

    @Test
    fun `classifies generic IOException as network error`() {
        assertEquals(ApiFailureReason.NETWORK_ERROR, IOException("no connectivity").toApiFailureReason())
    }

    @Test
    fun `classifies missing configuration as config error`() {
        assertEquals(ApiFailureReason.CONFIG_ERROR, IllegalStateException("falta la api key").toApiFailureReason())
    }

    @Test
    fun `classifies unrelated exceptions as unknown`() {
        assertEquals(ApiFailureReason.UNKNOWN, RuntimeException("algo inesperado").toApiFailureReason())
    }
}
