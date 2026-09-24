package com.example.smartbudget.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Income response DTO
 */
data class IncomeDto(
    @SerializedName("id")
    val id: String,
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
    @SerializedName("syncStatus")
    val syncStatus: String,
    @SerializedName("syncVersion")
    val syncVersion: Int,
    @SerializedName("createdAt")
    val createdAt: String, // ISO 8601 timestamp
    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Income list response DTO
 */
data class IncomeListResponse(
    @SerializedName("income")
    val income: List<IncomeDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)
