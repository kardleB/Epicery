package com.epicery.app.data.repository

import com.epicery.app.data.local.MonthlyBudgetDao
import com.epicery.app.data.local.MonthlyBudgetEntity
import com.epicery.app.domain.repository.MonthlyBudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class MonthlyBudgetRepositoryImpl @Inject constructor(
    private val dao: MonthlyBudgetDao
) : MonthlyBudgetRepository {

    override fun observeBudget(month: YearMonth): Flow<Double?> =
        dao.observe(month.toString()).map { it?.amount }

    override fun observePreviousBudget(month: YearMonth): Flow<Double?> =
        dao.observePreviousTo(month.toString()).map { it?.amount }

    override suspend fun setBudget(month: YearMonth, amount: Double) {
        require(amount > 0.0 && amount.isFinite()) { "El presupuesto mensual debe ser mayor que cero" }
        dao.upsert(MonthlyBudgetEntity(month.toString(), amount, System.currentTimeMillis()))
    }
}
