package com.epicery.app.ui.shoppinglist

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.ui.common.foodGroupAccentColor
import com.epicery.app.ui.common.foodGroupLabel
import com.epicery.app.ui.theme.EpiceryTheme

/**
 * Pantalla de Shopping List (ver `docs/design/wireframes.md`, sección "2. Shopping List"): lista
 * de compras semanal agrupada por [FoodGroup], con filtro por categoría ([CategoryFilter]), total
 * estimado vs. presupuesto ([BudgetHeader]) y alta/edición de items (CA1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<GroceryItem?>(null) }
    val context = LocalContext.current
    val groupLabels = FoodGroup.entries.associateWith { foodGroupLabel(it) }
    val totalLabel = stringResource(R.string.shopping_list_total_estimated_title)
    val shareChooserTitle = stringResource(R.string.shopping_list_share_chooser_title)
    val shareActionLabel = stringResource(R.string.shopping_list_share_action)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_list_title), style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = formatShoppingListForSharing(uiState.allItems, groupLabels, totalLabel)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, shareChooserTitle))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = shareActionLabel)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BudgetHeader(
                totalEstimated = uiState.totalEstimated,
                weeklyBudget = uiState.weeklyBudget,
                modifier = Modifier.fillMaxWidth()
            )
            CategoryFilter(
                selectedFoodGroup = uiState.selectedFoodGroup,
                onFoodGroupSelected = viewModel::selectFoodGroup,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            when {
                uiState.isLoading -> LoadingState()
                uiState.isEmpty -> EmptyState()
                else -> ShoppingListContent(
                    itemsByGroup = uiState.itemsByGroup,
                    onTogglePurchased = viewModel::togglePurchased,
                    onPriceClick = { editingItem = it }
                )
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, foodGroup, price ->
                viewModel.addItem(name, foodGroup, price)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        EditPriceDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { newPrice ->
                viewModel.updatePrice(item, newPrice)
                editingItem = null
            }
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.empty_add_first_item),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ShoppingListContent(
    itemsByGroup: Map<FoodGroup, List<GroceryItem>>,
    onTogglePurchased: (GroceryItem) -> Unit,
    onPriceClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FoodGroup.entries.forEach { group ->
            val groupItems = itemsByGroup[group].orEmpty()
            if (groupItems.isNotEmpty()) {
                item(key = "header_${group.name}") {
                    Text(
                        text = foodGroupLabel(group),
                        style = MaterialTheme.typography.titleLarge,
                        color = foodGroupAccentColor(group),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(groupItems, key = { it.id }) { groceryItem ->
                    FoodItemCard(
                        item = groceryItem,
                        foodGroup = group,
                        onTogglePurchased = { onTogglePurchased(groceryItem) },
                        onPriceClick = { onPriceClick(groceryItem) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
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
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

@Composable
private fun EditPriceDialog(
    item: GroceryItem,
    onDismiss: () -> Unit,
    onConfirm: (newPrice: Double) -> Unit
) {
    var priceText by rememberSaveable(item.id) { mutableStateOf(item.estimatedPrice.toString()) }
    val price = priceText.toDoubleOrNull()
    val isValid = price != null && price >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shopping_list_edit_price_title, item.name)) },
        text = {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text(stringResource(R.string.label_estimated_price)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = { onConfirm(price ?: item.estimatedPrice) }) {
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

@Preview(showBackground = true)
@Composable
private fun ShoppingListContentPreview() {
    EpiceryTheme {
        ShoppingListContent(
            itemsByGroup = mapOf(
                FoodGroup.FRUITS to listOf(GroceryItem(1, "Manzanas", "FRUITS", 5.0)),
                FoodGroup.PROTEIN to listOf(GroceryItem(2, "Pollo", "PROTEIN", 12.0, isPurchased = true))
            ),
            onTogglePurchased = {},
            onPriceClick = {}
        )
    }
}
