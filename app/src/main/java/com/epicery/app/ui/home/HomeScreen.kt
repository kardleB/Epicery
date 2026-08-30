package com.epicery.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.ui.common.foodGroupAccentColor
import com.epicery.app.ui.common.foodGroupLabel
import com.epicery.app.ui.theme.EpiceryTheme
import java.util.Locale

/**
 * Pantalla principal (ver `docs/design/wireframes.md`, sección "1. Home"): resumen del
 * presupuesto semanal, preview de la lista de compras activa y accesos a las demás secciones
 * (Shopping List, Price Tracker, Budget, Settings via bottom navigation en `EpiceryApp`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToShoppingList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuickAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showQuickAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(innerPadding)
            !uiState.hasActiveList -> EmptyState(innerPadding, onNavigateToShoppingList)
            else -> HomeContent(
                uiState = uiState,
                paddingValues = innerPadding,
                onSeeAllClick = onNavigateToShoppingList
            )
        }
    }

    if (showQuickAddDialog) {
        QuickAddItemDialog(
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { name, foodGroup, price ->
                viewModel.addQuickItem(name, foodGroup, price)
                showQuickAddDialog = false
            }
        )
    }
}

@Composable
private fun LoadingState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(paddingValues: PaddingValues, onCreateListClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateListClick) {
            Text(stringResource(R.string.home_empty_cta))
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onSeeAllClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WeeklyBudgetCard(amountSpent = uiState.amountSpent, weeklyBudget = uiState.weeklyBudget)
        }
        item {
            ShoppingListPreviewSection(
                items = uiState.shoppingListPreview,
                onSeeAllClick = onSeeAllClick
            )
        }
        item {
            FoodGroupsSection(counts = uiState.foodGroupCounts)
        }
    }
}

@Composable
private fun WeeklyBudgetCard(amountSpent: Double, weeklyBudget: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.home_weekly_budget_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (weeklyBudget > 0) (amountSpent / weeklyBudget).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (amountSpent > weeklyBudget && weeklyBudget > 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_weekly_budget_used, formatPrice(amountSpent), formatPrice(weeklyBudget)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShoppingListPreviewSection(
    items: List<GroceryItem>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.home_shopping_list_title), style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onSeeAllClick) {
                Text(stringResource(R.string.action_see_all))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_add_first_item),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item -> GroceryItemRow(item) }
            }
        }
    }
}

@Composable
private fun GroceryItemRow(item: GroceryItem, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        foodGroupOrNull(item.foodGroup)?.let { foodGroupAccentColor(it) }
                            ?: MaterialTheme.colorScheme.outline
                    )
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = formatPrice(item.estimatedPrice), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FoodGroupsSection(counts: Map<FoodGroup, Int>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.home_food_groups_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        FoodGroupChipsGrid(counts)
    }
}

@Composable
private fun FoodGroupChipsGrid(counts: Map<FoodGroup, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FoodGroup.entries.chunked(2).forEach { rowGroups ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowGroups.forEach { group ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${foodGroupLabel(group)} (${counts[group] ?: 0})") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = foodGroupAccentColor(group).copy(alpha = 0.15f),
                            labelColor = foodGroupAccentColor(group)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, foodGroup: FoodGroup, estimatedPrice: Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf(FoodGroup.FRUITS) }
    var expanded by remember { mutableStateOf(false) }

    val price = priceText.toDoubleOrNull()
    val isValid = name.isNotBlank() && price != null && price >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_item)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = foodGroupLabel(selectedGroup),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_food_group)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        FoodGroup.entries.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(foodGroupLabel(group)) },
                                onClick = {
                                    selectedGroup = group
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(stringResource(R.string.label_estimated_price)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(name.trim(), selectedGroup, price ?: 0.0) }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private fun foodGroupOrNull(value: String): FoodGroup? =
    runCatching { FoodGroup.valueOf(value.uppercase(Locale.ROOT)) }.getOrNull()

private fun formatPrice(amount: Double): String = "$${"%.2f".format(Locale.ROOT, amount)}"

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    EpiceryTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                hasActiveList = true,
                weeklyBudget = 120.0,
                amountSpent = 45.5,
                shoppingListPreview = listOf(
                    GroceryItem(1, "Manzanas", "FRUITS", 5.0),
                    GroceryItem(2, "Pollo", "PROTEIN", 12.0)
                ),
                foodGroupCounts = FoodGroup.entries.associateWith { 1 }
            ),
            paddingValues = PaddingValues(0.dp),
            onSeeAllClick = {}
        )
    }
}
