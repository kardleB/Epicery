package com.epicery.app.domain.repository

/**
 * Acceso al presupuesto semanal asociado a una lista de compras. Combina
 * el `ShoppingListDao` de Room (fuente de verdad local) con Firestore, para
 * sincronizar el presupuesto entre dispositivos cuando el usuario tiene
 * sesion iniciada.
 */
interface BudgetRepository {
    suspend fun getWeeklyBudget(shoppingListId: Long): Double
    suspend fun setWeeklyBudget(shoppingListId: Long, amount: Double)
    suspend fun syncWeeklyBudget(userId: String, shoppingListId: Long, amount: Double): Result<Unit>
    suspend fun fetchRemoteWeeklyBudget(userId: String, shoppingListId: Long): Double?
}
