package com.epicery.app.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.epicery.app.data.remote.ApiErrorState

/**
 * Banner de estado de error controlado (ver `ApiErrorState`): si la última llamada a
 * USDA FoodData o GroceryPulse falló (timeout, rate limiting, etc.), avisa que se está
 * mostrando datos locales en vez de dejar la falla silenciosa o tirar abajo la app.
 */
@Composable
fun ApiErrorBanner(modifier: Modifier = Modifier) {
    val lastError by ApiErrorState.lastError.collectAsState()
    lastError?.let { error ->
        Text(
            text = error.message,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(16.dp)
        )
    }
}
