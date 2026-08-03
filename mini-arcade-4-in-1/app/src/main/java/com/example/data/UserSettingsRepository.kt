package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val language: String = "TR",
    val blockTheme: String = "CLASSIC",
    val isLoggedIn: Boolean = false,
    val googleEmail: String = "",
    val googleName: String = "",
    val googlePhoto: String = ""
)

class UserSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val BLOCK_THEME = stringPreferencesKey("block_theme")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val GOOGLE_EMAIL = stringPreferencesKey("google_email")
        val GOOGLE_NAME = stringPreferencesKey("google_name")
        val GOOGLE_PHOTO = stringPreferencesKey("google_photo")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
            musicEnabled = preferences[PreferencesKeys.MUSIC_ENABLED] ?: true,
            darkMode = preferences[PreferencesKeys.DARK_MODE] ?: true,
            language = preferences[PreferencesKeys.LANGUAGE] ?: "TR",
            blockTheme = preferences[PreferencesKeys.BLOCK_THEME] ?: "CLASSIC",
            isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false,
            googleEmail = preferences[PreferencesKeys.GOOGLE_EMAIL] ?: "",
            googleName = preferences[PreferencesKeys.GOOGLE_NAME] ?: "",
            googlePhoto = preferences[PreferencesKeys.GOOGLE_PHOTO] ?: ""
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun setBlockTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLOCK_THEME] = theme
        }
    }

    suspend fun loginGoogle(email: String, name: String, photo: String = "") {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.GOOGLE_EMAIL] = email
            preferences[PreferencesKeys.GOOGLE_NAME] = name
            preferences[PreferencesKeys.GOOGLE_PHOTO] = photo
        }
    }

    suspend fun logoutGoogle() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
            preferences[PreferencesKeys.GOOGLE_EMAIL] = ""
            preferences[PreferencesKeys.GOOGLE_NAME] = ""
            preferences[PreferencesKeys.GOOGLE_PHOTO] = ""
        }
    }
}
