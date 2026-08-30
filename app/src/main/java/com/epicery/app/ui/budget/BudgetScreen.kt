package com.epicery.app.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.ui.theme.EpiceryTheme
import java.util.Locale

/**
 * Pantalla de Budget (ver `docs/design/wireframes.md`, sección "4. Budget"): dashboard del gasto
 * semanal acumulado de la lista activa ([BudgetProgress]), gráfico de gastos por categoría
 * ([CategorySpendingChart]) y proyección de gasto mensual ([MonthlyProjectionCard]), todo
 * actualizado en tiempo real a partir del `Flow` de items de la lista (RF4, CA3).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Presupuesto", style = MaterialTheme.typography.headlineSmall) })
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
            BudgetContent(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun BudgetContent(uiState: BudgetUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BudgetProgress(
            weeklySpent = uiState.weeklySpent,
            weeklyBudget = uiState.weeklyBudget,
            modifier = Modifier.fillMaxWidth()
        )
        CategorySpendingChart(
            spendingByCategory = uiState.spendingByCategory,
            modifier = Modifier.fillMaxWidth()
        )
        MonthlyProjectionCard(
            monthlyProjection = uiState.monthlyProjection,
            monthlyBudget = uiState.monthlyBudget,
            isOverProjection = uiState.isOverMonthlyProjection,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MonthlyProjectionCard(
    monthlyProjection: Double,
    monthlyBudget: Double,
    isOverProjection: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Proyección mensual", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${formatPrice(monthlyProjection)} estimados este mes, según el ritmo de gasto semanal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (monthlyBudget > 0) {
                Text(
                    text = "Presupuesto mensual estimado: ${formatPrice(monthlyBudget)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (isOverProjection) {
                    Text(
                        text = "La proyección supera el presupuesto mensual por " +
                            formatPrice(monthlyProjection - monthlyBudget),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"

@Preview(showBackground = true)
@Composable
private fun BudgetContentPreview() {
    EpiceryTheme {
        BudgetContent(
            uiState = BudgetUiState(
                isLoading = false,
                weeklyBudget = 100.0,
                weeklySpent = 65.0,
                spendingByCategory = mapOf(
                    FoodGroup.FRUITS to 20.0,
                    FoodGroup.PROTEIN to 45.0
                ),
                monthlyProjection = 65.0 * BudgetUiState.WEEKS_PER_MONTH
            )
        )
    }
}
