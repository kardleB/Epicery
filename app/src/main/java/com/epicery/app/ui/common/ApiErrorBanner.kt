package com.epicery.app.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicery.app.R
import com.epicery.app.data.remote.ApiErrorState
import com.epicery.app.data.remote.ApiFailureReason

/**
 * Banner de estado de error controlado (ver `ApiErrorState`): si la última llamada a
 * USDA FoodData o GroceryPulse falló (timeout, rate limiting, etc.), avisa que se está
 * mostrando datos locales en vez de dejar la falla silenciosa o tirar abajo la app. El mensaje
 * se resuelve desde [ApiFailureReason] (RF6) en vez de usar el `message` ya formateado del
 * evento, para que se muestre en el idioma activo de la app.
 */
@Composable
fun ApiErrorBanner(modifier: Modifier = Modifier) {
    val lastError by ApiErrorState.lastError.collectAsState()
    lastError?.let { error ->
        Text(
            text = stringResource(apiFailureMessageRes(error.reason)),
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(16.dp)
        )
    }
}

private fun apiFailureMessageRes(reason: ApiFailureReason): Int = when (reason) {
    ApiFailureReason.RATE_LIMITED -> R.string.api_error_rate_limited
    ApiFailureReason.TIMEOUT -> R.string.api_error_timeout
    ApiFailureReason.SERVER_ERROR -> R.string.api_error_server_error
    ApiFailureReason.CLIENT_ERROR -> R.string.api_error_client_error
    ApiFailureReason.NETWORK_ERROR -> R.string.api_error_network_error
    ApiFailureReason.CONFIG_ERROR -> R.string.api_error_config_error
    ApiFailureReason.UNKNOWN -> R.string.api_error_unknown
}
