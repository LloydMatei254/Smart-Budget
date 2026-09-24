package com.example.smartbudget.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Expense response DTO
 */
data class ExpenseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("amount")
    val amount: String, // String to preserve decimal precision
    @SerializedName("description")
    val description: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("date")
    val date: String, // YYYY-MM-DD format
    @SerializedName("category")
    val category: CategoryDto,
    @SerializedName("paymentMethod")
    val paymentMethod: PaymentMethodDto?,
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
 * Expense list response DTO
 */
data class ExpenseListResponse(
    @SerializedName("expenses")
    val expenses: List<ExpenseDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)

/**
 * Pagination DTO
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
