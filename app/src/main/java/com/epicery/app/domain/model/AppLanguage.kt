package com.epicery.app.domain.model

/**
 * Idiomas soportados por la pantalla de Settings (RF5). [code] es el codigo ISO 639-1
 * persistido en [com.epicery.app.domain.repository.SettingsRepository].
 */
enum class AppLanguage(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.find { it.code == code } ?: SPANISH
    }
}
