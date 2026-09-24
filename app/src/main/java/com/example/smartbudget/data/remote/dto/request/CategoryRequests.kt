package com.example.smartbudget.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Create category request DTO
 */
data class CreateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("color")
    val color: String, // Hex color code
    @SerializedName("icon")
    val icon: String // Material icon name
)

/**
 * Update category request DTO
 */
data class UpdateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("color")
    val color: String,
    @SerializedName("icon")
    val icon: String
)
