package com.epicery.app.data.repository

import com.epicery.app.data.local.ExpenseDao
import com.epicery.app.data.local.ExpenseEntity
import com.epicery.app.domain.model.Expense
import com.epicery.app.domain.model.GroceryItem
import com.epicery.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun observeExpenses(): Flow<List<Expense>> =
        dao.observeAll().map { entities ->
            entities.map { Expense(it.id, it.name, it.foodGroup, it.amount, it.purchasedAt) }
        }

    override suspend fun archivePurchased(items: List<GroceryItem>, nowMillis: Long): Int {
        val purchased = items.filter { it.isPurchased }
        if (purchased.isEmpty()) return 0
        dao.archive(
            expenses = purchased.map {
                ExpenseEntity(
                    name = it.name,
                    foodGroup = it.foodGroup,
                    amount = it.spentAmount,
                    purchasedAt = it.purchasedAt ?: nowMillis
                )
            },
            groceryItemIds = purchased.map { it.id }
        )
        return purchased.size
    }
}
