package com.epicery.app.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.calculator.MonthlyBudgetStatus
import com.epicery.app.domain.calculator.WeekSpending
import com.epicery.app.ui.theme.EpiceryTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de Budget (ver `docs/design/wireframes.md`, sección "4. Budget"): presupuesto del mes
 * ([MonthlyBudgetCard]), dashboard del gasto semanal ([BudgetProgress]), gráfico de gastos por
 * categoría ([CategorySpendingChart]) y proyección de gasto mensual ([MonthlyProjectionCard]),
 * todo actualizado en tiempo real a partir del `Flow` de items de la lista (RF4, CA3).
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
            TopAppBar(title = { Text(stringResource(R.string.budget_title), style = MaterialTheme.typography.headlineSmall) })
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
                onSaveMonthlyBudget = viewModel::saveMonthlyBudget,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun BudgetContent(
    uiState: BudgetUiState,
    onSaveMonthlyBudget: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthlyBudgetCard(
            uiState = uiState,
            onSaveMonthlyBudget = onSaveMonthlyBudget,
            modifier = Modifier.fillMaxWidth()
        )
        BudgetProgress(
            weeklySpent = uiState.weeklySpent,
            weeklyBudget = uiState.weeklyBudget,
            modifier = Modifier.fillMaxWidth()
        )
        CategorySpendingChart(
            spendingByCategory = uiState.spendingByCategory,
            modifier = Modifier.fillMaxWidth()
        )
        WeeklyExpensesCard(
            weeks = uiState.weeks,
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

/**
 * Lista "Gastos por semana": lo comprado en el mes agrupado de lunes a domingo, con el subtotal de
 * cada semana; al tocar una semana se ven sus compras.
 */
@Composable
private fun WeeklyExpensesCard(weeks: List<WeekSpending>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.budget_weeks_title), style = MaterialTheme.typography.titleMedium)
            if (weeks.isEmpty()) {
                Text(
                    text = stringResource(R.string.budget_weeks_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                weeks.forEach { week ->
                    WeekRow(week = week)
                }
            }
        }
    }
}

@Composable
private fun WeekRow(week: WeekSpending) {
    var expanded by rememberSaveable(week.start.toString()) { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.budget_week_range,
                        week.start.format(formatter),
                        week.end.format(formatter)
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.budget_week_items_count, week.entries.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = formatPrice(week.total), style = MaterialTheme.typography.titleMedium)
        }
        if (expanded) {
            week.entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatPrice(entry.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Presupuesto del mes: lo que se ha gastado frente al monto definido por el usuario, con aviso al
 * llegar al 80 % y al pasarse. Si todavia no hay presupuesto ofrece definirlo (o reutilizar el del
 * mes anterior).
 */
@Composable
private fun MonthlyBudgetCard(
    uiState: BudgetUiState,
    onSaveMonthlyBudget: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.budget_month_title), style = MaterialTheme.typography.titleMedium)
            uiState.month?.let { month ->
                Text(
                    text = monthLabel(month),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.isMonthlyBudgetSet) {
                val status = uiState.monthlyStatus
                val statusColor = when (status) {
                    MonthlyBudgetStatus.OVER -> MaterialTheme.colorScheme.error
                    MonthlyBudgetStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                LinearProgressIndicator(
                    progress = { uiState.monthlyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = statusColor
                )
                Text(
                    text = stringResource(
                        R.string.format_amount_of_amount,
                        formatPrice(uiState.monthlySpent),
                        formatPrice(uiState.monthlyBudget)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                val remaining = uiState.monthlyRemaining
                Text(
                    text = if (remaining >= 0) {
                        stringResource(R.string.budget_month_remaining, formatPrice(remaining))
                    } else {
                        stringResource(R.string.budget_month_over, formatPrice(-remaining))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (status == MonthlyBudgetStatus.ON_TRACK) MaterialTheme.colorScheme.onSurfaceVariant else statusColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (status == MonthlyBudgetStatus.WARNING) {
                    Text(
                        text = stringResource(R.string.budget_month_warning, uiState.monthlyPercentUsed),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showDialog = true }) {
                        Text(stringResource(R.string.budget_month_edit_button))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.budget_month_not_set),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(onClick = { showDialog = true }) {
                        Text(stringResource(R.string.budget_month_set_button))
                    }
                    if (uiState.previousMonthlyBudget > 0) {
                        TextButton(onClick = { onSaveMonthlyBudget(uiState.previousMonthlyBudget) }) {
                            Text(stringResource(R.string.budget_month_copy_previous, formatPrice(uiState.previousMonthlyBudget)))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        MonthlyBudgetDialog(
            initialAmount = if (uiState.isMonthlyBudgetSet) uiState.monthlyBudget else uiState.suggestedMonthlyBudget,
            isSuggestion = !uiState.isMonthlyBudgetSet && uiState.suggestedMonthlyBudget > 0,
            onConfirm = { amount ->
                onSaveMonthlyBudget(amount)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun MonthlyBudgetDialog(
    initialAmount: Double,
    isSuggestion: Boolean,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by rememberSaveable {
        mutableStateOf(if (initialAmount > 0) "%.2f".format(Locale.ROOT, initialAmount) else "")
    }
    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val isValid = amount != null && amount > 0 && amount.isFinite()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.budget_month_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.budget_month_dialog_label)) },
                    singleLine = true,
                    isError = amountText.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isSuggestion) {
                    Text(
                        text = stringResource(R.string.budget_month_dialog_suggestion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = { amount?.let(onConfirm) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun monthLabel(month: YearMonth): String {
    val locale = Locale.getDefault()
    return month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
        .replaceFirstChar { it.titlecase(locale) }
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
            Text(text = stringResource(R.string.budget_monthly_projection_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.budget_monthly_projection_body, formatPrice(monthlyProjection)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            if (monthlyBudget > 0) {
                Text(
                    text = stringResource(R.string.budget_monthly_budget_estimate, formatPrice(monthlyBudget)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (isOverProjection) {
                    Text(
                        text = stringResource(
                            R.string.budget_monthly_over,
                            formatPrice(monthlyProjection - monthlyBudget)
                        ),
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
                monthlyProjection = 65.0 * BudgetUiState.WEEKS_PER_MONTH,
                month = YearMonth.of(2026, 10),
                monthlyBudget = 400.0,
                monthlySpent = 330.0
            ),
            onSaveMonthlyBudget = {}
        )
    }
}
