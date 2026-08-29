package com.epicery.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ítem de una [ShoppingListEntity], vinculado al alimento del catálogo
 * ([FoodItemEntity]) que representa.
 */
@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shoppingListId"]), Index(value = ["foodItemId"])]
)
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shoppingListId: Long,
    val foodItemId: Long,
    @ColumnInfo(defaultValue = "1") val quantity: Double = 1.0,
    @ColumnInfo(defaultValue = "unidad") val unit: String = "unidad",
    @ColumnInfo(defaultValue = "0") val estimatedPrice: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val isPurchased: Boolean = false
)
