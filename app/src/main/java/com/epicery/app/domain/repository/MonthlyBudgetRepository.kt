package com.epicery.app.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/** Acceso al presupuesto mensual del usuario (guardado solo en el dispositivo). */
interface MonthlyBudgetRepository {
    /** Presupuesto de [month], o `null` si el usuario no lo ha definido. */
    fun observeBudget(month: YearMonth): Flow<Double?>

    /** Presupuesto del mes mas reciente anterior a [month], o `null` si no hay ninguno. */
    fun observePreviousBudget(month: YearMonth): Flow<Double?>

    /** Define o cambia el presupuesto de [month]; [amount] debe ser mayor que cero. */
    suspend fun setBudget(month: YearMonth, amount: Double)
}
