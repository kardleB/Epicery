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

    @Query("DELETE FROM grocery_items WHERE id IN (:ids)")
    abstract suspend fun deleteGroceryItems(ids: List<Long>)

    /**
     * Pasa los items comprados a gastos y los quita de la lista en una sola transaccion: o se hacen
     * las dos cosas o ninguna, asi una compra nunca queda duplicada ni se pierde.
     */
    @Transaction
    open suspend fun archive(expenses: List<ExpenseEntity>, groceryItemIds: List<Long>) {
        insertAll(expenses)
        deleteGroceryItems(groceryItemIds)
    }
}
