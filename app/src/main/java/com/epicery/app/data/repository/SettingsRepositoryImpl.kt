package com.epicery.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.epicery.app.domain.model.AppLanguage
import com.epicery.app.domain.model.UserSettings
import com.epicery.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val userSettings: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            language = AppLanguage.fromCode(preferences[LANGUAGE_KEY]),
            favoriteSupermarket = preferences[FAVORITE_SUPERMARKET_KEY] ?: "",
            defaultWeeklyBudget = preferences[DEFAULT_WEEKLY_BUDGET_KEY] ?: 0.0,
            useAppWithoutAccount = preferences[USE_APP_WITHOUT_ACCOUNT_KEY] ?: true
        )
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE_KEY] = language.code }
    }

    override suspend fun setFavoriteSupermarket(supermarket: String) {
        dataStore.edit { it[FAVORITE_SUPERMARKET_KEY] = supermarket }
    }

    override suspend fun setDefaultWeeklyBudget(amount: Double) {
        dataStore.edit { it[DEFAULT_WEEKLY_BUDGET_KEY] = amount }
    }

    override suspend fun setUseAppWithoutAccount(enabled: Boolean) {
        dataStore.edit { it[USE_APP_WITHOUT_ACCOUNT_KEY] = enabled }
    }

    private companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val FAVORITE_SUPERMARKET_KEY = stringPreferencesKey("favorite_supermarket")
        val DEFAULT_WEEKLY_BUDGET_KEY = doublePreferencesKey("default_weekly_budget")
        val USE_APP_WITHOUT_ACCOUNT_KEY = booleanPreferencesKey("use_app_without_account")
    }
}
