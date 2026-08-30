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

    /**
     * Codigo de idioma (RF6) tal como fue persistido, o `null` si el usuario nunca lo cambio
     * explicitamente desde Settings. Se usa para aplicar el override de idioma de la app
     * (`AppCompatDelegate.setApplicationLocales` en `EpiceryApplication`) sin pisar el idioma
     * del dispositivo (CA4) hasta que el usuario elija uno.
     */
    val storedLanguageCode: Flow<String?>

    suspend fun setLanguage(language: AppLanguage)
    suspend fun setFavoriteSupermarket(supermarket: String)
    suspend fun setDefaultWeeklyBudget(amount: Double)
    suspend fun setUseAppWithoutAccount(enabled: Boolean)
}
