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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.ui.theme.EpiceryTheme
import com.epicery.app.ui.theme.FoodGroupDairy
import com.epicery.app.ui.theme.FoodGroupFruits
import com.epicery.app.ui.theme.FoodGroupGrains
import com.epicery.app.ui.theme.FoodGroupProtein
import com.epicery.app.ui.theme.FoodGroupVegetables
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
            Text(text = "Gasto por categoría", style = MaterialTheme.typography.titleMedium)
            if (spendingByCategory.isEmpty()) {
                Text(
                    text = "Todavía no marcaste ningún item como comprado",
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
            Text(text = categoryLabel(group), style = MaterialTheme.typography.bodyMedium)
            Text(text = formatPrice(amount), style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(categoryColor(group).copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(categoryColor(group))
            )
        }
    }
}

private fun categoryColor(group: FoodGroup): Color = when (group) {
    FoodGroup.FRUITS -> FoodGroupFruits
    FoodGroup.VEGETABLES -> FoodGroupVegetables
    FoodGroup.GRAINS -> FoodGroupGrains
    FoodGroup.PROTEIN -> FoodGroupProtein
    FoodGroup.DAIRY -> FoodGroupDairy
}

private fun categoryLabel(group: FoodGroup): String = when (group) {
    FoodGroup.FRUITS -> "Frutas"
    FoodGroup.VEGETABLES -> "Vegetales"
    FoodGroup.GRAINS -> "Granos"
    FoodGroup.PROTEIN -> "Proteínas"
    FoodGroup.DAIRY -> "Lácteos"
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
