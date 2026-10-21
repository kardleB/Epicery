package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY purchasedAt DESC, id DESC")
    abstract fun observeAll(): Flow<List<ExpenseEntity>>

    @Insert
    abstract suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query(
        "UPDATE grocery_items SET isPurchased = 0, purchasedAt = NULL, pricePaid = NULL WHERE id IN (:ids)"
    )
    abstract suspend fun resetGroceryItems(ids: List<Long>)

    /**
     * Guarda las compras como gastos y deja esos items en la lista, desmarcados y con el precio que
     * el usuario puso, en una sola transaccion: o se hacen las dos cosas o ninguna, asi una compra
     * nunca se cuenta dos veces ni se pierde.
     */
    @Transaction
    open suspend fun archive(expenses: List<ExpenseEntity>, groceryItemIds: List<Long>) {
        insertAll(expenses)
        resetGroceryItems(groceryItemIds)
    }
}
