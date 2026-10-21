package com.epicery.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth")
    fun observe(yearMonth: String): Flow<MonthlyBudgetEntity?>

    /** Presupuesto del mes mas reciente anterior a [yearMonth], para ofrecer reutilizarlo. */
    @Query("SELECT * FROM monthly_budgets WHERE yearMonth < :yearMonth ORDER BY yearMonth DESC LIMIT 1")
    fun observePreviousTo(yearMonth: String): Flow<MonthlyBudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: MonthlyBudgetEntity)
}
