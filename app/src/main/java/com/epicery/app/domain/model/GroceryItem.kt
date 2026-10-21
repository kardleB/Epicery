package com.epicery.app.domain.model

data class GroceryItem(
    val id: Long = 0,
    val name: String,
    val foodGroup: String,
    val estimatedPrice: Double,
    val isPurchased: Boolean = false,
    val purchasedAt: Long? = null,
    val pricePaid: Double? = null
) {
    /** Monto que cuenta contra el presupuesto: el precio pagado si se registro, si no el estimado. */
    val spentAmount: Double get() = pricePaid ?: estimatedPrice
}
