package com.example.smartbudget.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Login request DTO
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

/**
 * Register request DTO
 */
data class RegisterRequest(
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("currency")
    val currency: String
)

/**
 * Refresh token request DTO
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

/**
 * Change password request DTO
 */
data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)

/**
 * Update profile request DTO
 */
data class UpdateProfileRequest(
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("currency")
    val currency: String?
)

/**
 * Logout request DTO
 */
data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)
