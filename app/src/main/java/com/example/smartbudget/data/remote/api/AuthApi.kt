package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.request.ChangePasswordRequest
import com.example.smartbudget.data.remote.dto.request.LoginRequest
import com.example.smartbudget.data.remote.dto.request.LogoutRequest
import com.example.smartbudget.data.remote.dto.request.RefreshTokenRequest
import com.example.smartbudget.data.remote.dto.request.RegisterRequest
import com.example.smartbudget.data.remote.dto.request.UpdateProfileRequest
import com.example.smartbudget.data.remote.dto.response.AuthResponse
import com.example.smartbudget.data.remote.dto.response.RefreshTokenResponse
import com.example.smartbudget.data.remote.dto.response.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit API interface for authentication endpoints
 */
interface AuthApi {
    
    /**
     * Register a new user
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<AuthResponse>
    
    /**
     * Login with email and password
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<AuthResponse>
    
    /**
     * Refresh access token
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiResponse<RefreshTokenResponse>
    
    /**
     * Logout current user
     */
    @POST("auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): ApiResponse<Unit>
    
    /**
     * Get current user profile
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): ApiResponse<UserDto>
    
    /**
     * Update user profile
     */
    @PUT("auth/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): ApiResponse<UserDto>
    
    /**
     * Change password
     */
    @POST("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): ApiResponse<Unit>
}
