package com.epicery.app.domain.repository

import com.epicery.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Cuenta opcional del usuario (RF5): la app funciona por completo sin cuenta
 * ([com.epicery.app.domain.model.UserSettings.useAppWithoutAccount]), esto solo habilita
 * iniciar/cerrar sesión desde Settings para quien quiera identificarse.
 */
interface AuthRepository {

    /** Emite el usuario autenticado actual, o null si no hay sesión iniciada. */
    val authState: Flow<AuthUser?>

    suspend fun signIn(email: String, password: String): Result<AuthUser>

    suspend fun signUp(email: String, password: String): Result<AuthUser>

    fun signOut()
}
