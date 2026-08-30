package com.epicery.app.ui.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.epicery.app.R
import com.epicery.app.ui.theme.EpiceryTheme
import java.util.Locale

/**
 * Dashboard del gasto semanal acumulado de la lista activa vs. el presupuesto configurado por el
 * usuario (RF4, CA3: "el dashboard refleja en tiempo real el gasto acumulado de la lista
 * activa"). [weeklySpent] son los items ya marcados como comprados.
 */
@Composable
fun BudgetProgress(
    weeklySpent: Double,
    weeklyBudget: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.budget_weekly_spent_title), style = MaterialTheme.typography.titleMedium)
            val progress = if (weeklyBudget > 0) {
                (weeklySpent / weeklyBudget).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }
            val isOverBudget = weeklyBudget > 0 && weeklySpent > weeklyBudget
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.format_amount_of_amount, formatPrice(weeklySpent), formatPrice(weeklyBudget)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (weeklyBudget > 0) {
                val remaining = weeklyBudget - weeklySpent
                Text(
                    text = if (remaining >= 0) {
                        stringResource(R.string.budget_remaining, formatPrice(remaining))
                    } else {
                        stringResource(R.string.budget_over, formatPrice(-remaining))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"

@Preview(showBackground = true)
@Composable
private fun BudgetProgressPreview() {
    EpiceryTheme {
        BudgetProgress(weeklySpent = 65.0, weeklyBudget = 100.0)
    }
}
