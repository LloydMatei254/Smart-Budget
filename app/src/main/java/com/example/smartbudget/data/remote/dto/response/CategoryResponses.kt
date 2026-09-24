package com.example.smartbudget.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Category response DTO
 */
data class CategoryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("color")
    val color: String, // Hex color code
    @SerializedName("icon")
    val icon: String, // Material icon name
    @SerializedName("isDefault")
    val isDefault: Boolean,
    @SerializedName("expenseCount")
    val expenseCount: Int? = null,
    @SerializedName("totalSpent")
    val totalSpent: String? = null,
    @SerializedName("createdAt")
    val createdAt: String, // ISO 8601 timestamp
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Payment method response DTO
 */
data class PaymentMethodDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("isDefault")
    val isDefault: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)
