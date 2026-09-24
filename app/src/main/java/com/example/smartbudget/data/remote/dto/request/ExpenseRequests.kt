package com.example.smartbudget.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Create expense request DTO
 */
data class CreateExpenseRequest(
    @SerializedName("amount")
    val amount: String, // String to preserve decimal precision
    @SerializedName("description")
    val description: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("date")
    val date: String, // YYYY-MM-DD format
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("paymentMethodId")
    val paymentMethodId: String?,
    @SerializedName("localId")
    val localId: String? = null
)

/**
 * Update expense request DTO
 */
data class UpdateExpenseRequest(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("date")
    val date: String,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("paymentMethodId")
    val paymentMethodId: String?,
    @SerializedName("syncVersion")
    val syncVersion: Int = 1
)
