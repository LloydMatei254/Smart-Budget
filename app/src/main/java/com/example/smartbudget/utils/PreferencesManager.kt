package com.example.smartbudget.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for app preferences using DataStore
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_CURRENCY_KEY = stringPreferencesKey("user_currency")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val LAST_SYNC_TIMESTAMP_KEY = longPreferencesKey("last_sync_timestamp")
        private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
    }

    /**
     * Save user ID
     */
    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
        Timber.d("User ID saved")
    }

    /**
     * Get user ID
     */
    fun getUserId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }
    
    /**
     * Get user ID synchronously (for repository use)
     * Returns null if not available
     */
    suspend fun getUserIdSync(): String? {
        return try {
            dataStore.data.map { preferences ->
                preferences[USER_ID_KEY]
            }.first()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user ID")
            null
        }
    }

    /**
     * Save user email
     */
    suspend fun saveUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    /**
     * Get user email
     */
    fun getUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    /**
     * Save user name
     */
    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    /**
     * Get user name
     */
    fun getUserName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }
    }

    /**
     * Save user currency
     */
    suspend fun saveUserCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[USER_CURRENCY_KEY] = currency
        }
    }

    /**
     * Get user currency
     */
    fun getUserCurrency(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_CURRENCY_KEY] ?: "USD"
        }
    }

    /**
     * Save login state
     */
    suspend fun saveLoginState(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
        Timber.d("Login state saved: $isLoggedIn")
    }

    /**
     * Get login state
     */
    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }
    }

    /**
     * Save last sync timestamp
     */
    suspend fun saveLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_KEY] = timestamp
        }
    }

    /**
     * Get last sync timestamp
     */
    fun getLastSyncTimestamp(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[LAST_SYNC_TIMESTAMP_KEY]
        }
    }

    /**
     * Save remember me preference
     */
    suspend fun saveRememberMe(rememberMe: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = rememberMe
        }
    }

    /**
     * Get remember me preference
     */
    fun getRememberMe(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[REMEMBER_ME_KEY] ?: false
        }
    }

    /**
     * Clear all preferences (logout)
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        Timber.d("All preferences cleared")
    }

    /**
     * Clear user session data (keep remember me)
     */
    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_CURRENCY_KEY)
            preferences.remove(IS_LOGGED_IN_KEY)
            // Keep REMEMBER_ME_KEY
        }
        Timber.d("User session cleared")
    }
}
