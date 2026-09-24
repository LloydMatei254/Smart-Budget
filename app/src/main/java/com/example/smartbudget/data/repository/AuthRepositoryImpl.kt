package com.example.smartbudget.data.repository

import com.example.smartbudget.data.local.dao.UserDao
import com.example.smartbudget.data.mapper.UserMapper.toDomain
import com.example.smartbudget.data.mapper.UserMapper.toEntity
import com.example.smartbudget.data.remote.api.AuthApi
import com.example.smartbudget.data.remote.dto.request.ChangePasswordRequest
import com.example.smartbudget.data.remote.dto.request.LoginRequest
import com.example.smartbudget.data.remote.dto.request.LogoutRequest
import com.example.smartbudget.data.remote.dto.request.RefreshTokenRequest
import com.example.smartbudget.data.remote.dto.request.RegisterRequest
import com.example.smartbudget.data.remote.dto.request.UpdateProfileRequest
import com.example.smartbudget.domain.model.User
import com.example.smartbudget.domain.repository.AuthRepository
import com.example.smartbudget.utils.PreferencesManager
import com.example.smartbudget.utils.SecureStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of AuthRepository
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val secureStorage: SecureStorage,
    private val preferencesManager: PreferencesManager,
    private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        currency: String
    ): Result<User> = withContext(ioDispatcher) {
        try {
            val request = RegisterRequest(
                fullName = fullName,
                email = email,
                password = password,
                currency = currency
            )
            
            val response = authApi.register(request)
            
            if (response.success && response.data != null) {
                val authData = response.data
                val user = authData.user.toDomain()
                
                // Save tokens securely
                secureStorage.saveTokens(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    expiresIn = authData.expiresIn
                )
                
                // Save user data locally
                userDao.insertUser(user.toEntity())
                
                // Save user preferences
                preferencesManager.saveUserId(user.id)
                preferencesManager.saveUserEmail(user.email)
                preferencesManager.saveUserName(user.fullName)
                preferencesManager.saveUserCurrency(user.currency.code)
                preferencesManager.saveLoginState(true)
                
                Timber.d("User registered successfully: ${user.email}")
                Result.success(user)
            } else {
                val errorMessage = response.error?.message ?: "Registration failed"
                Timber.e("Registration failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Registration error")
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<User> = withContext(ioDispatcher) {
        try {
            val request = LoginRequest(
                email = email,
                password = password
            )
            
            val response = authApi.login(request)
            
            if (response.success && response.data != null) {
                val authData = response.data
                val user = authData.user.toDomain()
                
                // Save tokens securely
                secureStorage.saveTokens(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    expiresIn = authData.expiresIn
                )
                
                // Save user data locally
                userDao.insertUser(user.toEntity())
                
                // Save user preferences
                preferencesManager.saveUserId(user.id)
                preferencesManager.saveUserEmail(user.email)
                preferencesManager.saveUserName(user.fullName)
                preferencesManager.saveUserCurrency(user.currency.code)
                preferencesManager.saveLoginState(true)
                preferencesManager.saveRememberMe(rememberMe)
                
                Timber.d("User logged in successfully: ${user.email}")
                Result.success(user)
            } else {
                val errorMessage = response.error?.message ?: "Login failed"
                Timber.e("Login failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Get refresh token for logout request
            val refreshToken = secureStorage.getRefreshToken()
            
            if (refreshToken != null) {
                try {
                    val request = LogoutRequest(refreshToken = refreshToken)
                    authApi.logout(request)
                    Timber.d("Logout request sent to server")
                } catch (e: Exception) {
                    Timber.w(e, "Failed to notify server of logout, continuing with local cleanup")
                }
            }
            
            // Clear tokens
            secureStorage.clearTokens()
            
            // Clear user data
            userDao.deleteAllUsers()
            
            // Clear preferences
            preferencesManager.clearUserSession()
            
            Timber.d("User logged out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> = withContext(ioDispatcher) {
        try {
            // First try to get from local database
            val localUser = userDao.getCurrentUser()
            if (localUser != null) {
                Timber.d("Retrieved user from local database")
                return@withContext Result.success(localUser.toDomain())
            }
            
            // If not in local database, try to fetch from API
            if (secureStorage.hasValidTokens()) {
                try {
                    val response = authApi.getCurrentUser()
                    if (response.success && response.data != null) {
                        val user = response.data.toDomain()
                        userDao.insertUser(user.toEntity())
                        Timber.d("Fetched and cached user from API")
                        return@withContext Result.success(user)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to fetch user from API")
                }
            }
            
            Result.success(null)
        } catch (e: Exception) {
            Timber.e(e, "Error getting current user")
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val refreshToken = secureStorage.getRefreshToken()
            
            if (refreshToken == null) {
                Timber.e("No refresh token available")
                return@withContext Result.failure(Exception("No refresh token available"))
            }
            
            val request = RefreshTokenRequest(refreshToken = refreshToken)
            val response = authApi.refreshToken(request)
            
            if (response.success && response.data != null) {
                val tokenData = response.data
                
                secureStorage.saveTokens(
                    accessToken = tokenData.accessToken,
                    refreshToken = tokenData.refreshToken,
                    expiresIn = tokenData.expiresIn
                )
                
                Timber.d("Token refreshed successfully")
                Result.success(Unit)
            } else {
                val errorMessage = response.error?.message ?: "Token refresh failed"
                Timber.e("Token refresh failed: $errorMessage")
                
                // If refresh fails, logout the user
                logout()
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Token refresh error")
            
            // If refresh fails, logout the user
            logout()
            
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return preferencesManager.isLoggedIn()
    }

    override suspend fun updateProfile(
        fullName: String?,
        currency: String?
    ): Result<User> = withContext(ioDispatcher) {
        try {
            val request = UpdateProfileRequest(
                fullName = fullName,
                currency = currency
            )
            
            val response = authApi.updateProfile(request)
            
            if (response.success && response.data != null) {
                val user = response.data.toDomain()
                
                // Update local database
                userDao.insertUser(user.toEntity())
                
                // Update preferences
                preferencesManager.saveUserName(user.fullName)
                preferencesManager.saveUserCurrency(user.currency.code)
                
                Timber.d("Profile updated successfully")
                Result.success(user)
            } else {
                val errorMessage = response.error?.message ?: "Profile update failed"
                Timber.e("Profile update failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Profile update error")
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            
            val response = authApi.changePassword(request)
            
            if (response.success) {
                Timber.d("Password changed successfully")
                Result.success(Unit)
            } else {
                val errorMessage = response.error?.message ?: "Password change failed"
                Timber.e("Password change failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Password change error")
            Result.failure(e)
        }
    }

    override suspend fun ensureValidToken(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (secureStorage.isTokenExpired()) {
                Timber.d("Token expired, refreshing...")
                return@withContext refreshToken()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error checking token validity")
            Result.failure(e)
        }
    }
}
