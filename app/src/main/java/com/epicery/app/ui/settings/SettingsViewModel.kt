package com.epicery.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.domain.model.AppLanguage
import com.epicery.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pantalla de Settings (RF5): expone y actualiza las preferencias de usuario persistidas en
 * [SettingsRepository] (DataStore local), para que sobrevivan entre sesiones sin requerir una
 * cuenta (RNF4).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.userSettings
        .map { settings ->
            SettingsUiState(
                isLoading = false,
                language = settings.language,
                favoriteSupermarket = settings.favoriteSupermarket,
                defaultWeeklyBudget = settings.defaultWeeklyBudget,
                useAppWithoutAccount = settings.useAppWithoutAccount
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsUiState()
        )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setFavoriteSupermarket(supermarket: String) {
        viewModelScope.launch { settingsRepository.setFavoriteSupermarket(supermarket) }
    }

    fun setDefaultWeeklyBudget(amount: Double) {
        viewModelScope.launch { settingsRepository.setDefaultWeeklyBudget(amount) }
    }

    fun setUseAppWithoutAccount(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUseAppWithoutAccount(enabled) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
