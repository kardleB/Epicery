package com.epicery.app.data.repository

import com.epicery.app.data.local.MonthlyBudgetDao
import com.epicery.app.data.local.MonthlyBudgetEntity
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/** Verifica que [MonthlyBudgetRepositoryImpl] guarde por mes, reemplace y encuentre el mes anterior. */
class MonthlyBudgetRepositoryImplTest {

    private class FakeMonthlyBudgetDao : MonthlyBudgetDao {
        private val rows = MutableStateFlow<Map<String, MonthlyBudgetEntity>>(emptyMap())

        override fun observe(yearMonth: String): Flow<MonthlyBudgetEntity?> = rows.map { it[yearMonth] }

        override fun observePreviousTo(yearMonth: String): Flow<MonthlyBudgetEntity?> =
            rows.map { all -> all.values.filter { it.yearMonth < yearMonth }.maxByOrNull { it.yearMonth } }

        override suspend fun upsert(budget: MonthlyBudgetEntity) {
            rows.value = rows.value + (budget.yearMonth to budget)
        }
    }

    private val october = YearMonth.of(2026, 10)

    @Test
    fun `there is no budget until the user defines one`() = runBlocking {
        val repository = MonthlyBudgetRepositoryImpl(FakeMonthlyBudgetDao())

        assertNull(repository.observeBudget(october).first())
    }

    @Test
    fun `saves the budget of a month and reads it back`() = runBlocking {
        val repository = MonthlyBudgetRepositoryImpl(FakeMonthlyBudgetDao())

        repository.setBudget(october, 450.0)

        assertEquals(450.0, repository.observeBudget(october).first()!!, 0.0001)
    }

    @Test
    fun `changing the budget replaces the old amount and does not touch other months`() = runBlocking {
        val repository = MonthlyBudgetRepositoryImpl(FakeMonthlyBudgetDao())
        repository.setBudget(YearMonth.of(2026, 9), 300.0)
        repository.setBudget(october, 450.0)

        repository.setBudget(october, 500.0)

        assertEquals(500.0, repository.observeBudget(october).first()!!, 0.0001)
        assertEquals(300.0, repository.observeBudget(YearMonth.of(2026, 9)).first()!!, 0.0001)
    }

    @Test
    fun `previous budget is the most recent earlier month, across a year change`() = runBlocking {
        val repository = MonthlyBudgetRepositoryImpl(FakeMonthlyBudgetDao())
        repository.setBudget(YearMonth.of(2025, 11), 280.0)
        repository.setBudget(YearMonth.of(2025, 12), 320.0)
        repository.setBudget(YearMonth.of(2026, 3), 999.0)

        assertEquals(320.0, repository.observePreviousBudget(YearMonth.of(2026, 1)).first()!!, 0.0001)
        assertNull(repository.observePreviousBudget(YearMonth.of(2025, 11)).first())
    }

    @Test
    fun `rejects budgets that are not greater than zero`() {
        val repository = MonthlyBudgetRepositoryImpl(FakeMonthlyBudgetDao())

        assertThrows(IllegalArgumentException::class.java) { runBlocking { repository.setBudget(october, 0.0) } }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { repository.setBudget(october, -5.0) } }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { repository.setBudget(october, Double.NaN) } }
    }
}
