package com.epicery.app.data.remote

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Punto único de acceso a Firebase (Auth, Firestore y Analytics) dentro del plan gratuito
 * (Spark): Auth por email/password, sincronización de documentos en Firestore y logging
 * básico de eventos de Analytics.
 */
@Singleton
class FirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val analytics: FirebaseAnalytics,
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun authStateChanges(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth -> trySend(firebaseAuth.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user ?: error("No se pudo crear el usuario")
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user ?: error("No se pudo autenticar al usuario")
    }

    fun signOut() = auth.signOut()

    /** Sube (crea o sobrescribe) un documento para sincronizar datos locales con Firestore. */
    suspend fun syncDocument(collection: String, documentId: String, data: Map<String, Any?>): Result<Unit> =
        runCatching {
            suspendCancellableCoroutine { continuation ->
                firestore.collection(collection).document(documentId).set(data)
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }
        }

    /** Descarga un documento previamente sincronizado; null si no existe. */
    suspend fun fetchDocument(collection: String, documentId: String): Result<Map<String, Any?>?> = runCatching {
        suspendCancellableCoroutine { continuation ->
            firestore.collection(collection).document(documentId).get()
                .addOnSuccessListener { continuation.resume(it.data) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        val bundle = android.os.Bundle()
        params.forEach { (key, value) -> bundle.putString(key, value) }
        analytics.logEvent(name, bundle)
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { continuation.resume(it) }
            addOnFailureListener { continuation.resumeWithException(it) }
        }
}
