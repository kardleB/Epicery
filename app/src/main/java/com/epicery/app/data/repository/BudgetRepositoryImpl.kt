package com.epicery.app.data.repository

import com.epicery.app.data.local.ShoppingListDao
import com.epicery.app.data.remote.FirebaseService
import com.epicery.app.domain.repository.BudgetRepository
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val firebaseService: FirebaseService
) : BudgetRepository {

    override suspend fun getWeeklyBudget(shoppingListId: Long): Double =
        shoppingListDao.getListById(shoppingListId)?.estimatedBudget ?: 0.0

    override suspend fun setWeeklyBudget(shoppingListId: Long, amount: Double) {
        val list = shoppingListDao.getListById(shoppingListId) ?: return
        shoppingListDao.updateList(list.copy(estimatedBudget = amount))
    }

    override suspend fun syncWeeklyBudget(userId: String, shoppingListId: Long, amount: Double): Result<Unit> =
        firebaseService.syncDocument(
            collection = WEEKLY_BUDGETS_COLLECTION,
            documentId = remoteDocumentId(userId, shoppingListId),
            data = mapOf("shoppingListId" to shoppingListId, "amount" to amount)
        )

    override suspend fun fetchRemoteWeeklyBudget(userId: String, shoppingListId: Long): Double? =
        firebaseService.fetchDocument(
            collection = WEEKLY_BUDGETS_COLLECTION,
            documentId = remoteDocumentId(userId, shoppingListId)
        ).getOrNull()?.get("amount") as? Double

    private fun remoteDocumentId(userId: String, shoppingListId: Long) = "$userId-$shoppingListId"

    private companion object {
        const val WEEKLY_BUDGETS_COLLECTION = "weekly_budgets"
    }
}
