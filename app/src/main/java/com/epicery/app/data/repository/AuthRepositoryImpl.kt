package com.epicery.app.data.repository

import com.epicery.app.data.remote.FirebaseService
import com.epicery.app.domain.model.AuthUser
import com.epicery.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : AuthRepository {

    override val authState: Flow<AuthUser?> =
        firebaseService.authStateChanges().map { it?.toAuthUser() }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> =
        firebaseService.signIn(email, password).map { it.toAuthUser() }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> =
        firebaseService.signUp(email, password).map { it.toAuthUser() }

    override fun signOut() = firebaseService.signOut()

    private fun FirebaseUser.toAuthUser() = AuthUser(uid = uid, email = email)
}
