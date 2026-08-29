package com.epicery.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lista de compras (por ejemplo, la lista semanal) creada por el usuario.
 */
@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val estimatedBudget: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val isCompleted: Boolean = false
)
