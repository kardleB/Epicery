package com.epicery.app.ui.shoppinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epicery.app.data.local.FoodGroup

/**
 * Fila de filtros por grupo alimenticio (CA1: "filtrar por categoría alimenticia"). `null`
 * representa "Todos" y vuelve a mostrar todos los grupos.
 */
@Composable
fun CategoryFilter(
    selectedFoodGroup: FoodGroup?,
    onFoodGroupSelected: (FoodGroup?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFoodGroup == null,
                onClick = { onFoodGroupSelected(null) },
                label = { Text("Todos") },
                shape = RoundedCornerShape(20.dp)
            )
        }
        items(FoodGroup.entries) { group ->
            FilterChip(
                selected = selectedFoodGroup == group,
                onClick = {
                    onFoodGroupSelected(if (selectedFoodGroup == group) null else group)
                },
                label = { Text(displayName(group)) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor(group).copy(alpha = 0.2f),
                    selectedLabelColor = accentColor(group)
                )
            )
        }
    }
}
