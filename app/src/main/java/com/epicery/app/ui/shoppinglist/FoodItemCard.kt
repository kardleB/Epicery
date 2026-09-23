package com.epicery.app.ui.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.epicery.app.R
import com.epicery.app.data.local.FoodGroup
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.ui.common.foodGroupAccentColor

/**
 * Fila de un [GroceryItem] dentro de la lista agrupada por [FoodGroup] (CA1: marcar/desmarcar
 * comprado y editar el item ya creado -- nombre, categoría y precio). El ícono de lápiz abre el
 * diálogo de edición completo: no se edita inline para evitar teclados accidentales al scrollear.
 */
@Composable
fun FoodItemCard(
    item: GroceryItem,
    foodGroup: FoodGroup?,
    onTogglePurchased: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(checked = item.isPurchased, onCheckedChange = { onTogglePurchased() })
            if (foodGroup != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(foodGroupAccentColor(foodGroup))
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                    color = if (item.isPurchased) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatPrice(item.estimatedPrice),
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.shopping_list_edit_item_action)
                )
            }
        }
    }
}
