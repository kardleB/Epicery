package com.epicery.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.epicery.app.domain.model.AppLanguage
import com.epicery.app.ui.theme.EpiceryTheme

/**
 * Pantalla de Settings (ver `docs/design/wireframes.md`): permite elegir idioma, supermercado
 * favorito, presupuesto semanal por defecto y si la app se usa sin cuenta (solo datos locales).
 * Los cuatro ajustes se persisten en [SettingsViewModel] via DataStore, por lo que sobreviven
 * entre sesiones sin depender de un login (RF5, RNF4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Ajustes", style = MaterialTheme.typography.headlineSmall) })
        }
    ) { innerPadding ->
        SettingsContent(
            uiState = uiState,
            onLanguageSelected = viewModel::setLanguage,
            onFavoriteSupermarketSelected = viewModel::setFavoriteSupermarket,
            onDefaultWeeklyBudgetChanged = viewModel::setDefaultWeeklyBudget,
            onUseAppWithoutAccountChanged = viewModel::setUseAppWithoutAccount,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onFavoriteSupermarketSelected: (String) -> Unit,
    onDefaultWeeklyBudgetChanged: (Double) -> Unit,
    onUseAppWithoutAccountChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Preferencias", style = MaterialTheme.typography.titleMedium)

                LanguageDropdown(
                    selectedLanguage = uiState.language,
                    onLanguageSelected = onLanguageSelected
                )

                FavoriteSupermarketDropdown(
                    selectedSupermarket = uiState.favoriteSupermarket,
                    onSupermarketSelected = onFavoriteSupermarketSelected
                )

                DefaultWeeklyBudgetField(
                    defaultWeeklyBudget = uiState.defaultWeeklyBudget,
                    onDefaultWeeklyBudgetChanged = onDefaultWeeklyBudgetChanged
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(text = "Usar la app sin cuenta", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Los datos se guardan solo en este dispositivo, sin sincronizar con la nube",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.useAppWithoutAccount,
                    onCheckedChange = onUseAppWithoutAccountChanged
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLanguage.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Idioma") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteSupermarketDropdown(
    selectedSupermarket: String,
    onSupermarketSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedSupermarket.ifBlank { "Sin definir" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Supermercado favorito") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MONTREAL_SUPERMARKETS.forEach { supermarket ->
                DropdownMenuItem(
                    text = { Text(supermarket) },
                    onClick = {
                        onSupermarketSelected(supermarket)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DefaultWeeklyBudgetField(
    defaultWeeklyBudget: Double,
    onDefaultWeeklyBudgetChanged: (Double) -> Unit
) {
    var budgetText by rememberSaveable(defaultWeeklyBudget) {
        mutableStateOf(if (defaultWeeklyBudget > 0) defaultWeeklyBudget.toString() else "")
    }
    val parsedBudget = budgetText.toDoubleOrNull()
    val isValid = parsedBudget != null && parsedBudget >= 0

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it },
            label = { Text("Presupuesto semanal por defecto") },
            singleLine = true,
            isError = budgetText.isNotBlank() && !isValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(
                enabled = isValid && parsedBudget != defaultWeeklyBudget,
                onClick = { parsedBudget?.let(onDefaultWeeklyBudgetChanged) }
            ) {
                Text("Guardar")
            }
        }
    }
}

private val MONTREAL_SUPERMARKETS = listOf(
    "Metro",
    "Provigo",
    "IGA",
    "Walmart",
    "Costco",
    "Maxi",
    "Super C"
)

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    EpiceryTheme {
        SettingsContent(
            uiState = SettingsUiState(
                isLoading = false,
                language = AppLanguage.SPANISH,
                favoriteSupermarket = "Metro",
                defaultWeeklyBudget = 100.0,
                useAppWithoutAccount = true
            ),
            onLanguageSelected = {},
            onFavoriteSupermarketSelected = {},
            onDefaultWeeklyBudgetChanged = {},
            onUseAppWithoutAccountChanged = {}
        )
    }
}
