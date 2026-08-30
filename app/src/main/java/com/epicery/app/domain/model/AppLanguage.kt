package com.epicery.app.domain.model

/**
 * Idiomas soportados por la app (RF6, CA4). [code] es el codigo ISO 639-1 persistido en
 * [com.epicery.app.domain.repository.SettingsRepository] y aplicado como override de idioma via
 * `AppCompatDelegate.setApplicationLocales` en `EpiceryApplication`. El frances es el idioma
 * predeterminado (mercado objetivo: Quebec).
 */
enum class AppLanguage(val code: String, val displayName: String) {
    FRENCH("fr", "Français"),
    ENGLISH("en", "English"),
    SPANISH("es", "Español");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.find { it.code == code } ?: FRENCH
    }
}
