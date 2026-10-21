package com.epicery.app.domain.repository

import com.epicery.app.domain.model.Expense
import com.epicery.app.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

/** Compras ya terminadas ("Gastos por semana"), guardadas solo en el dispositivo. */
interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>

    /**
     * Termina la compra: guarda como gastos los items de [items] que estan marcados como comprados
     * (con el precio pagado si existe, si no el estimado) y los quita de la lista, todo en una
     * transaccion. Un item marcado sin fecha usa [nowMillis]. Devuelve cuantos items se archivaron.
     */
    suspend fun archivePurchased(items: List<GroceryItem>, nowMillis: Long): Int
}
