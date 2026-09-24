package com.example.smartbudget.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Create income request DTO
 */
data class CreateIncomeRequest(
    @SerializedName("amount")
    val amount: String, // String to preserve decimal precision
    @SerializedName("source")
    val source: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("date")
    val date: String, // YYYY-MM-DD format
    @SerializedName("localId")
    val localId: String? = null
)

/**
 * Update income request DTO
 */
data class UpdateIncomeRequest(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("date")
    val date: String,
    @SerializedName("syncVersion")
    val syncVersion: Int = 1
)
