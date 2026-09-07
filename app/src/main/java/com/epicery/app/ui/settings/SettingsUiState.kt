package com.epicery.app.ui.settings

import com.epicery.app.domain.model.AppLanguage
import com.epicery.app.domain.model.AuthUser

/**
 * Estado de la pantalla Settings (RF5): idioma, supermercado favorito, presupuesto semanal
 * por defecto y si la app se usa sin cuenta (solo datos locales). Se recalcula a partir del
 * `Flow` de [com.epicery.app.domain.repository.SettingsRepository] para reflejar en tiempo
 * real lo persistido entre sesiones (RNF4). [authUser] es independiente de lo anterior: la
 * cuenta (Firebase Auth) es opcional, ver [com.epicery.app.domain.repository.AuthRepository].
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val language: AppLanguage = AppLanguage.FRENCH,
    val favoriteSupermarket: String = "",
    val defaultWeeklyBudget: Double = 0.0,
    val useAppWithoutAccount: Boolean = true,
    val authUser: AuthUser? = null,
    val isAuthLoading: Boolean = false,
    val authError: String? = null
)
