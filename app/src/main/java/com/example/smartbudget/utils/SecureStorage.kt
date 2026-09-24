package com.example.smartbudget.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage for sensitive data using EncryptedSharedPreferences
 * backed by Android Keystore
 */
@Singleton
class SecureStorage @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val SECURE_PREFS_FILE = "smart_budget_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create EncryptedSharedPreferences")
            throw e
        }
    }

    /**
     * Save access token
     */
    suspend fun saveAccessToken(token: String) {
        try {
            encryptedPrefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
            Timber.d("Access token saved securely")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save access token")
        }
    }

    /**
     * Get access token
     */
    suspend fun getAccessToken(): String? {
        return try {
            encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve access token")
            null
        }
    }

    /**
     * Save refresh token
     */
    suspend fun saveRefreshToken(token: String) {
        try {
            encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
            Timber.d("Refresh token saved securely")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save refresh token")
        }
    }

    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(): String? {
        return try {
            encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve refresh token")
            null
        }
    }

    /**
     * Save token expiry timestamp
     */
    suspend fun saveTokenExpiry(expiryTimestamp: Long) {
        try {
            encryptedPrefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTimestamp).apply()
            Timber.d("Token expiry saved")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save token expiry")
        }
    }

    /**
     * Get token expiry timestamp
     */
    suspend fun getTokenExpiry(): Long {
        return try {
            encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve token expiry")
            0L
        }
    }

    /**
     * Check if access token is expired
     */
    suspend fun isTokenExpired(): Boolean {
        val expiry = getTokenExpiry()
        val now = System.currentTimeMillis()
        return expiry <= now
    }

    /**
     * Save both tokens and expiry
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Int) {
        val expiryTimestamp = System.currentTimeMillis() + (expiresIn * 1000L)
        saveAccessToken(accessToken)
        saveRefreshToken(refreshToken)
        saveTokenExpiry(expiryTimestamp)
    }

    /**
     * Clear all tokens (logout)
     */
    suspend fun clearTokens() {
        try {
            encryptedPrefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRY)
                .apply()
            Timber.d("All tokens cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear tokens")
        }
    }

    /**
     * Check if user has valid tokens
     */
    suspend fun hasValidTokens(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return accessToken != null && refreshToken != null && !isTokenExpired()
    }
}
