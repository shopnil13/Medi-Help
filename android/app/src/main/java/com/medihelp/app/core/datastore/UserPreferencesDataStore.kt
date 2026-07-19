package com.medihelp.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "medi_help_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    val displayName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DISPLAY_NAME]
    }

    val healthConnectSyncEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HEALTH_CONNECT_SYNC_ENABLED] ?: false
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETE] = true }
    }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { prefs -> prefs[KEY_DISPLAY_NAME] = name }
    }

    suspend fun setHealthConnectSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_HEALTH_CONNECT_SYNC_ENABLED] = enabled }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }

    private companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_HEALTH_CONNECT_SYNC_ENABLED = booleanPreferencesKey("health_connect_sync_enabled")
    }
}
