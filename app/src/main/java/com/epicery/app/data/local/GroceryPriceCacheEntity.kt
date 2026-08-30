package com.epicery.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Caché persistida de cotizaciones de GroceryPulse (Apify), indexada por el término de
 * búsqueda normalizado (RNF5: soporte offline-first, evita repetir llamadas de red ya
 * resueltas recientemente). Se guarda una fila por combinación artículo/supermercado,
 * igual que hace el actor de Apify al responder.
 */
@Entity(
    tableName = "grocery_price_cache",
    indices = [Index(value = ["query"])]
)
data class GroceryPriceCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val storeName: String,
    val productName: String,
    val price: Double,
    val currency: String,
    val city: String,
    val sourceUrl: String?,
    val fetchedAt: Long
)
