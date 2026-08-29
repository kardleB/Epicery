package com.epicery.app.domain.model

data class GroceryItem(
    val id: Long = 0,
    val name: String,
    val foodGroup: String,
    val estimatedPrice: Double
)
