package com.epicery.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Compra ya terminada (el usuario pulso "Terminar compra"): se copia del item de la lista con el
 * monto realmente gastado y sale de la lista activa. La semana y el mes se calculan a partir de
 * [purchasedAt], asi no hay datos duplicados que puedan quedar desfasados.
 */
@Entity(tableName = "expenses", indices = [Index(value = ["purchasedAt"])])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val foodGroup: String,
    val amount: Double,
    val purchasedAt: Long
)
