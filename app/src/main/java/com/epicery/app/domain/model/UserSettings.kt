package com.epicery.app.domain.model

/**
 * Preferencias del usuario configurables desde la pantalla de Settings (RF5), persistidas
 * localmente via [com.epicery.app.domain.repository.SettingsRepository] para que sobrevivan
 * entre sesiones sin requerir una cuenta (RNF4).
 */
data class UserSettings(
    val language: AppLanguage = AppLanguage.FRENCH,
    val favoriteSupermarket: String = "",
    val defaultWeeklyBudget: Double = 0.0,
    val useAppWithoutAccount: Boolean = true
)
