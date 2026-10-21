package com.epicery.app.domain.model

/** Compra terminada: un item de la lista que ya se pago y salio de ella. */
data class Expense(
    val id: Long = 0,
    val name: String,
    val foodGroup: String,
    val amount: Double,
    val purchasedAt: Long
)

/**
 * Una linea de gasto, venga de un item recien marcado en la lista ([isArchived] = false) o de una
 * compra ya terminada ([isArchived] = true). Es la unidad con la que se calculan los gastos por
 * semana y por mes.
 */
data class SpendingEntry(
    val name: String,
    val foodGroup: String,
    val amount: Double,
    val purchasedAt: Long,
    val isArchived: Boolean
)
