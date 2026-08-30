package com.epicery.app.ui.shoppinglist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.epicery.app.R

/**
 * Total estimado de la lista vs. presupuesto semanal (CA1: "ver el total estimado"), fijo en la
 * parte superior de la pantalla para que quede visible mientras se scrollea la lista.
 */
@Composable
fun BudgetHeader(
    totalEstimated: Double,
    weeklyBudget: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.shopping_list_total_estimated_title), style = MaterialTheme.typography.titleMedium)
            val progress = if (weeklyBudget > 0) {
                (totalEstimated / weeklyBudget).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (totalEstimated > weeklyBudget && weeklyBudget > 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Text(
                text = stringResource(R.string.format_amount_of_amount, formatPrice(totalEstimated), formatPrice(weeklyBudget)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
