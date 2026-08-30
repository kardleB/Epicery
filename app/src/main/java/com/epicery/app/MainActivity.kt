package com.epicery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.epicery.app.data.remote.ApiErrorState
import com.epicery.app.ui.theme.EpiceryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EpiceryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        ApiErrorBanner()
                        Greeting()
                    }
                }
            }
        }
    }
}

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

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(text = "Hello World", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EpiceryTheme {
        Greeting()
    }
}
