package com.epicery.app.domain.repository

import com.epicery.app.domain.model.AppLanguage
import com.epicery.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a las preferencias de usuario de la pantalla Settings (RF5). Se persisten solo
 * localmente (DataStore Preferences) para que la app funcione sin cuenta y los ajustes
 * sobrevivan entre sesiones (RNF4).
 */
interface SettingsRepository {
    val userSettings: Flow<UserSettings>

    suspend fun setLanguage(language: AppLanguage)
    suspend fun setFavoriteSupermarket(supermarket: String)
    suspend fun setDefaultWeeklyBudget(amount: Double)
    suspend fun setUseAppWithoutAccount(enabled: Boolean)
}
