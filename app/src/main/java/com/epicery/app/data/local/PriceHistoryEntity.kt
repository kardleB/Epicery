package com.epicery.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro histórico de precios de un [FoodItemEntity] en un comercio,
 * usado para el tracking de precios y la estimación de presupuesto semanal.
 */
@Entity(
    tableName = "price_history",
    foreignKeys = [
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["foodItemId"])]
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodItemId: Long,
    val storeName: String,
    val price: Double,
    @ColumnInfo(defaultValue = "CAD") val currency: String = "CAD",
    val recordedAt: Long
)
