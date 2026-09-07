package com.epicery.app.domain.model

/**
 * Usuario autenticado (Firebase Auth), expuesto al dominio/UI sin acoplarlos al SDK de Firebase.
 */
data class AuthUser(
    val uid: String,
    val email: String?
)
