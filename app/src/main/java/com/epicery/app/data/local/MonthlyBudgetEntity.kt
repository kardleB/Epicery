package com.epicery.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Presupuesto de un mes. [yearMonth] tiene el formato `yyyy-MM` (por ejemplo `2026-10`), que ademas
 * de identificar el mes se ordena cronologicamente como texto.
 */
@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
    @PrimaryKey val yearMonth: String,
    val amount: Double,
    val updatedAt: Long
)
