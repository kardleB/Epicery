package com.epicery.app.ui.pricetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.FoodItem
import com.epicery.app.domain.model.PriceAlert
import com.epicery.app.domain.model.PriceHistory
import com.epicery.app.ui.theme.EpiceryTheme
import java.util.Locale

/**
 * Pantalla de Price Tracker (ver `docs/design/wireframes.md`, sección "3. Price Tracker"):
 * selector de producto sobre el catálogo, gráfico de tendencia de precios ([PriceChart]) y
 * alerta cuando el precio actual supera el promedio histórico (RF3, CA2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceTrackerScreen(
    modifier: Modifier = Modifier,
    viewModel: PriceTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.price_tracker_title), style = MaterialTheme.typography.headlineSmall) })
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PriceTrackerContent(
                uiState = uiState,
                onFoodItemSelected = viewModel::selectFoodItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun PriceTrackerContent(
    uiState: PriceTrackerUiState,
    onFoodItemSelected: (FoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProductSelector(
            foodItems = uiState.foodItems,
            selectedFoodItem = uiState.selectedFoodItem,
            onFoodItemSelected = onFoodItemSelected,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            uiState.selectedFoodItem == null -> HintState(stringResource(R.string.price_tracker_hint_select_product))
            !uiState.hasHistory -> HintState(
                stringResource(R.string.price_tracker_hint_no_history, uiState.selectedFoodItem.name)
            )
            else -> {
                uiState.priceAlert?.let { alert ->
                    HighPriceAlert(
                        latestPrice = alert.currentPrice,
                        averagePrice = alert.averagePrice
                    )
                }
                TrendSummary(
                    foodItemName = uiState.selectedFoodItem.name,
                    trend = uiState.trend,
                    latestPrice = uiState.latestPrice ?: 0.0,
                    averagePrice = uiState.averagePrice
                )
                PriceChart(
                    priceHistory = uiState.priceHistory,
                    trend = uiState.trend,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSelector(
    foodItems: List<FoodItem>,
    selectedFoodItem: FoodItem?,
    onFoodItemSelected: (FoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedFoodItem?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_product)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            foodItems.forEach { foodItem ->
                DropdownMenuItem(
                    text = { Text(foodItem.name) },
                    onClick = {
                        onFoodItemSelected(foodItem)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HighPriceAlert(latestPrice: Double, averagePrice: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = stringResource(
                    R.string.price_tracker_high_price_alert,
                    formatPrice(latestPrice),
                    formatPrice(averagePrice)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun TrendSummary(
    foodItemName: String,
    trend: PriceTrend,
    latestPrice: Double,
    averagePrice: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = foodItemName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = trendIcon(trend),
                    contentDescription = trendLabel(trend),
                    tint = trendColor(trend)
                )
                Text(
                    text = stringResource(R.string.price_tracker_trend_label, trendLabel(trend)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = trendColor(trend)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.price_tracker_latest_avg,
                    formatPrice(latestPrice),
                    formatPrice(averagePrice)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HintState(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun trendIcon(trend: PriceTrend) = when (trend) {
    PriceTrend.UP -> Icons.Default.TrendingUp
    PriceTrend.DOWN -> Icons.Default.TrendingDown
    PriceTrend.STABLE -> Icons.Default.TrendingFlat
}

@Composable
private fun trendLabel(trend: PriceTrend): String = stringResource(
    when (trend) {
        PriceTrend.UP -> R.string.trend_up
        PriceTrend.DOWN -> R.string.trend_down
        PriceTrend.STABLE -> R.string.trend_stable
    }
)

@Composable
private fun trendColor(trend: PriceTrend) = when (trend) {
    PriceTrend.UP -> MaterialTheme.colorScheme.error
    PriceTrend.DOWN -> MaterialTheme.colorScheme.primary
    PriceTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"

@Preview(showBackground = true)
@Composable
private fun PriceTrackerContentPreview() {
    EpiceryTheme {
        PriceTrackerContent(
            uiState = PriceTrackerUiState(
                isLoading = false,
                foodItems = listOf(FoodItem(1, "Manzanas", FoodGroup.FRUITS, "Frutas")),
                selectedFoodItem = FoodItem(1, "Manzanas", FoodGroup.FRUITS, "Frutas"),
                priceHistory = listOf(
                    PriceHistory(foodItemId = 1, storeName = "Metro", price = 3.5, recordedAt = 1L),
                    PriceHistory(foodItemId = 1, storeName = "Metro", price = 3.8, recordedAt = 2L),
                    PriceHistory(foodItemId = 1, storeName = "Metro", price = 4.5, recordedAt = 3L)
                ),
                averagePrice = 3.93,
                latestPrice = 4.5,
                trend = PriceTrend.UP,
                priceAlert = PriceAlert(itemName = "Manzanas", currentPrice = 4.5, averagePrice = 3.65, increaseRatio = 0.233)
            ),
            onFoodItemSelected = {}
        )
    }
}
