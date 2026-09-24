package com.example.smartbudget.domain.repository

import com.example.smartbudget.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    
    /**
     * Register a new user
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        currency: String
    ): Result<User>
    
    /**
     * Login with email and password
     */
    suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Result<User>
    
    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): Result<User?>
    
    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshToken(): Result<Unit>
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Flow<Boolean>
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(
        fullName: String?,
        currency: String?
    ): Result<User>
    
    /**
     * Change password
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit>
    
    /**
     * Check if access token is expired and refresh if needed
     */
    suspend fun ensureValidToken(): Result<Unit>
}
