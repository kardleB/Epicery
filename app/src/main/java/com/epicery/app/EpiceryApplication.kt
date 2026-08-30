package com.epicery.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.epicery.app.domain.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Aplica el idioma persistido en Settings (RF6, CA4) como override de la app entera via
 * `AppCompatDelegate.setApplicationLocales`. Si el usuario nunca eligio un idioma en Settings,
 * [SettingsRepository.storedLanguageCode] emite `null` y no se fuerza ningun override: la app
 * sigue el idioma del dispositivo (con frances como fallback de recursos para idiomas no
 * soportados, ver `res/values/strings.xml`). Reacciona en vivo a cambios desde SettingsViewModel,
 * asi que no hace falta reiniciar la app para ver el nuevo idioma.
 */
@HiltAndroidApp
class EpiceryApplication : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        settingsRepository.storedLanguageCode
            .onEach { languageCode ->
                val locales = if (languageCode != null) {
                    LocaleListCompat.forLanguageTags(languageCode)
                } else {
                    LocaleListCompat.getEmptyLocaleList()
                }
                AppCompatDelegate.setApplicationLocales(locales)
            }
            .launchIn(applicationScope)
    }
}
