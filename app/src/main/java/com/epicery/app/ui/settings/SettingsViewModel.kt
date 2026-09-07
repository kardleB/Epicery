package com.epicery.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicery.app.domain.model.AppLanguage
import com.epicery.app.domain.repository.AuthRepository
import com.epicery.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pantalla de Settings (RF5): expone y actualiza las preferencias de usuario persistidas en
 * [SettingsRepository] (DataStore local), para que sobrevivan entre sesiones sin requerir una
 * cuenta (RNF4). También expone la cuenta opcional (Firebase Auth) vía [AuthRepository]: es
 * independiente de las preferencias locales, que siguen funcionando con o sin sesión iniciada.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val authUiState = MutableStateFlow(AuthUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.userSettings,
        authRepository.authState,
        authUiState
    ) { settings, authUser, authUi ->
        SettingsUiState(
            isLoading = false,
            language = settings.language,
            favoriteSupermarket = settings.favoriteSupermarket,
            defaultWeeklyBudget = settings.defaultWeeklyBudget,
            useAppWithoutAccount = settings.useAppWithoutAccount,
            authUser = authUser,
            isAuthLoading = authUi.isLoading,
            authError = authUi.error
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

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authUiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signIn(email, password)
                .onFailure { error -> authUiState.update { it.copy(error = error.message) } }
            authUiState.update { it.copy(isLoading = false) }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            authUiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signUp(email, password)
                .onFailure { error -> authUiState.update { it.copy(error = error.message) } }
            authUiState.update { it.copy(isLoading = false) }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun dismissAuthError() {
        authUiState.update { it.copy(error = null) }
    }

    private data class AuthUiState(val isLoading: Boolean = false, val error: String? = null)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
