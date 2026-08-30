package com.epicery.app.data.remote

import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

/**
 * Clasifica por qué falló una llamada a una API externa (USDA FoodData, GroceryPulse),
 * para decidir el mensaje de fallback al degradar a datos locales/cacheados (ver
 * `UsdaFoodDataRepositoryImpl` y `GroceryPulseRepositoryImpl`).
 */
enum class ApiFailureReason {
    RATE_LIMITED,
    TIMEOUT,
    SERVER_ERROR,
    CLIENT_ERROR,
    NETWORK_ERROR,
    CONFIG_ERROR,
    UNKNOWN
}

fun Throwable.toApiFailureReason(): ApiFailureReason = when {
    this is SocketTimeoutException -> ApiFailureReason.TIMEOUT
    this is HttpException && code() == 429 -> ApiFailureReason.RATE_LIMITED
    this is HttpException && code() in 500..599 -> ApiFailureReason.SERVER_ERROR
    this is HttpException -> ApiFailureReason.CLIENT_ERROR
    this is IllegalStateException -> ApiFailureReason.CONFIG_ERROR
    this is IOException -> ApiFailureReason.NETWORK_ERROR
    else -> ApiFailureReason.UNKNOWN
}

/** Mensaje de fallback listo para mostrar al usuario cuando se degrada a datos locales. */
fun ApiFailureReason.toFallbackMessage(): String = when (this) {
    ApiFailureReason.RATE_LIMITED -> "Se alcanzó el límite de consultas a la API. Mostrando datos guardados localmente."
    ApiFailureReason.TIMEOUT -> "La API tardó demasiado en responder. Mostrando datos guardados localmente."
    ApiFailureReason.SERVER_ERROR -> "La API no está disponible en este momento. Mostrando datos guardados localmente."
    ApiFailureReason.CLIENT_ERROR -> "No se pudo completar la consulta a la API. Mostrando datos guardados localmente."
    ApiFailureReason.NETWORK_ERROR -> "Sin conexión a internet. Mostrando datos guardados localmente."
    ApiFailureReason.CONFIG_ERROR -> "La API no está configurada. Mostrando datos guardados localmente."
    ApiFailureReason.UNKNOWN -> "No se pudo completar la consulta a la API. Mostrando datos guardados localmente."
}
