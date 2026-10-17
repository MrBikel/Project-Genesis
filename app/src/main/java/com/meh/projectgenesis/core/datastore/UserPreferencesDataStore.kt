package com.meh.projectgenesis.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.meh.projectgenesis.domain.model.AutonomyLevel
import com.meh.projectgenesis.domain.model.ReasoningMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "genesis_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_REASONING_MODE = stringPreferencesKey("reasoning_mode")
        val KEY_AUTONOMY_LEVEL = intPreferencesKey("autonomy_level")
        val KEY_EMERGENCY_STOP = booleanPreferencesKey("emergency_stop")
        val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_DEMO_MODE = booleanPreferencesKey("demo_mode")
        val KEY_WEB_RETRIEVAL_ALLOWED = booleanPreferencesKey("web_retrieval_allowed")
        val KEY_BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "bn"
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "DARK"
    }

    val reasoningModeFlow: Flow<ReasoningMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_REASONING_MODE] ?: ReasoningMode.BALANCED.name
        try {
            ReasoningMode.valueOf(raw)
        } catch (_: Exception) {
            ReasoningMode.BALANCED
        }
    }

    val autonomyLevelFlow: Flow<AutonomyLevel> = context.dataStore.data.map { prefs ->
        val levelInt = prefs[KEY_AUTONOMY_LEVEL] ?: 2
        AutonomyLevel.entries.firstOrNull { it.level == levelInt } ?: AutonomyLevel.LEVEL_2
    }

    val emergencyStopFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMERGENCY_STOP] ?: false
    }

    val aiProviderFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AI_PROVIDER] ?: "LOCAL"
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val demoModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEMO_MODE] ?: true
    }

    val webRetrievalAllowedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WEB_RETRIEVAL_ALLOWED] ?: false
    }

    val biometricLockFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_LOCK] ?: false
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setReasoningMode(mode: ReasoningMode) {
        context.dataStore.edit { it[KEY_REASONING_MODE] = mode.name }
    }

    suspend fun setAutonomyLevel(level: AutonomyLevel) {
        context.dataStore.edit { it[KEY_AUTONOMY_LEVEL] = level.level }
    }

    suspend fun setEmergencyStop(active: Boolean) {
        context.dataStore.edit { it[KEY_EMERGENCY_STOP] = active }
    }

    suspend fun setAiProvider(provider: String) {
        context.dataStore.edit { it[KEY_AI_PROVIDER] = provider }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDemoMode(demo: Boolean) {
        context.dataStore.edit { it[KEY_DEMO_MODE] = demo }
    }

    suspend fun setWebRetrievalAllowed(allowed: Boolean) {
        context.dataStore.edit { it[KEY_WEB_RETRIEVAL_ALLOWED] = allowed }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_LOCK] = enabled }
    }
}
