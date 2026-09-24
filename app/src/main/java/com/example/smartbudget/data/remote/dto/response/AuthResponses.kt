package com.example.smartbudget.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Authentication response DTO
 */
data class AuthResponse(
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("expiresIn")
    val expiresIn: Int // Seconds until expiration
)

/**
 * Token refresh response DTO
 */
data class RefreshTokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("expiresIn")
    val expiresIn: Int
)

/**
 * User DTO
 */
data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("createdAt")
    val createdAt: String, // ISO 8601 timestamp
    @SerializedName("updatedAt")
    val updatedAt: String
)
