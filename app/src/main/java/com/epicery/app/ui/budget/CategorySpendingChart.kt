package com.epicery.app.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.ui.common.foodGroupAccentColor
import com.epicery.app.ui.common.foodGroupLabel
import com.epicery.app.ui.theme.EpiceryTheme
import java.util.Locale

/**
 * Gráfico de barras del gasto acumulado por categoría (RF4, CA3), como barras horizontales
 * proporcionales al gasto de cada [FoodGroup] frente a la categoría de mayor gasto.
 */
@Composable
fun CategorySpendingChart(
    spendingByCategory: Map<FoodGroup, Double>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.budget_spending_by_category_title), style = MaterialTheme.typography.titleMedium)
            if (spendingByCategory.isEmpty()) {
                Text(
                    text = stringResource(R.string.budget_no_purchases),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                val maxAmount = spendingByCategory.values.max()
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FoodGroup.entries.forEach { group ->
                        val amount = spendingByCategory[group] ?: return@forEach
                        CategoryBar(
                            group = group,
                            amount = amount,
                            fraction = (amount / maxAmount).toFloat().coerceIn(0f, 1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(group: FoodGroup, amount: Double, fraction: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = foodGroupLabel(group), style = MaterialTheme.typography.bodyMedium)
            Text(text = formatPrice(amount), style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(foodGroupAccentColor(group).copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(foodGroupAccentColor(group))
            )
        }
    }
}

private fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"

@Preview(showBackground = true)
@Composable
private fun CategorySpendingChartPreview() {
    EpiceryTheme {
        CategorySpendingChart(
            spendingByCategory = mapOf(
                FoodGroup.FRUITS to 12.0,
                FoodGroup.PROTEIN to 25.0,
                FoodGroup.DAIRY to 8.0
            )
        )
    }
}
