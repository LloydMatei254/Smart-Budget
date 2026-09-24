package com.example.smartbudget.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("error")
    val error: ApiError? = null
)

/**
 * API error details
 */
data class ApiError(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("fields")
    val fields: Map<String, String>? = null,
    @SerializedName("details")
    val details: Map<String, Any>? = null
)

/**
 * Pagination information
 */
data class PaginationDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean
)

/**
 * Paginated list response
 */
data class PaginatedResponse<T>(
    @SerializedName("items")
    val items: List<T>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)
