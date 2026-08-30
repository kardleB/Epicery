package com.epicery.app.data.remote

import com.epicery.app.util.Constants
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Reintenta llamadas a APIs externas (USDA FoodData, GroceryPulse) ante fallas transitorias,
 * en vez de dejar que se propaguen como un timeout o un rate limit sin control:
 *  - Errores de red/timeout (IOException al ejecutar el request).
 *  - HTTP 429 (rate limiting, ej. el límite de 1000 req/hora de USDA) y 5xx (error del
 *    servidor). Si el servidor manda `Retry-After`, se respeta ese tiempo de espera;
 *    si no, se usa backoff exponencial.
 *
 * Otros códigos 4xx (ej. 401, 404) no se reintentan: repetir la misma request no los va
 * a resolver.
 */
class RetryInterceptor(
    private val maxRetries: Int = Constants.API_MAX_RETRIES,
    private val baseDelayMs: Long = Constants.API_RETRY_BASE_DELAY_MS
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastIoException: IOException? = null

        while (attempt <= maxRetries) {
            var response: Response? = null
            try {
                response = chain.proceed(chain.request())
                if (response.isSuccessful || !response.isRetryable() || attempt == maxRetries) {
                    return response
                }
                val delayMs = response.retryAfterMillis() ?: backoffDelayMs(attempt)
                response.close()
                Thread.sleep(delayMs)
            } catch (e: IOException) {
                response?.close()
                lastIoException = e
                if (attempt == maxRetries) throw e
                Thread.sleep(backoffDelayMs(attempt))
            }
            attempt++
        }

        throw lastIoException ?: IOException("Reintentos agotados sin respuesta")
    }

    private fun Response.isRetryable(): Boolean = code == 429 || code in 500..599

    private fun Response.retryAfterMillis(): Long? =
        header("Retry-After")?.trim()?.toLongOrNull()?.let { it * 1000 }

    private fun backoffDelayMs(attempt: Int): Long = baseDelayMs * (1L shl attempt)
}
