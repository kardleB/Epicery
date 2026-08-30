package com.epicery.app.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Un fallo controlado de una llamada a una API externa, listo para mostrarse al usuario. */
data class ApiErrorEvent(
    val source: String,
    val reason: ApiFailureReason,
    val message: String = reason.toFallbackMessage()
)

/**
 * Último error de red/API conocido, expuesto como estado observable para que la UI pueda
 * mostrarlo (ej. un banner) sin que la app se caiga ni interrumpa el flujo con datos locales.
 * Los repositorios (`UsdaFoodDataRepositoryImpl`, `GroceryPulseRepositoryImpl`) lo actualizan
 * cuando degradan a cache/local por un timeout, un error del servidor o rate limiting.
 */
object ApiErrorState {
    private val _lastError = MutableStateFlow<ApiErrorEvent?>(null)
    val lastError: StateFlow<ApiErrorEvent?> = _lastError

    fun report(source: String, reason: ApiFailureReason) {
        _lastError.value = ApiErrorEvent(source, reason)
    }

    fun clear() {
        _lastError.value = null
    }
}
