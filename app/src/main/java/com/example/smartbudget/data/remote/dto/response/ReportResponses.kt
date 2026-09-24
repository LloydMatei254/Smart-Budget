package com.example.smartbudget.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Financial summary response DTO
 */
data class FinancialSummaryDto(
    @SerializedName("totalIncome")
    val totalIncome: String,
    @SerializedName("totalExpenses")
    val totalExpenses: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("incomeCount")
    val incomeCount: Int,
    @SerializedName("expenseCount")
    val expenseCount: Int,
    @SerializedName("averageExpense")
    val averageExpense: String?,
    @SerializedName("largestExpense")
    val largestExpense: LargestExpenseDto?,
    @SerializedName("period")
    val period: PeriodDto?
)

/**
 * Largest expense DTO
 */
data class LargestExpenseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("date")
    val date: String
)

/**
 * Period DTO
 */
data class PeriodDto(
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String
)

/**
 * Spending by category response DTO
 */
data class SpendingByCategoryResponse(
    @SerializedName("categories")
    val categories: List<CategorySpendingDto>,
    @SerializedName("totalSpent")
    val totalSpent: String,
    @SerializedName("period")
    val period: PeriodDto?
)

/**
 * Category spending DTO
 */
data class CategorySpendingDto(
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("categoryColor")
    val categoryColor: String,
    @SerializedName("categoryIcon")
    val categoryIcon: String,
    @SerializedName("totalAmount")
    val totalAmount: String,
    @SerializedName("transactionCount")
    val transactionCount: Int,
    @SerializedName("percentage")
    val percentage: Float,
    @SerializedName("averageAmount")
    val averageAmount: String?
)

/**
 * Monthly trend response DTO
 */
data class MonthlyTrendResponse(
    @SerializedName("months")
    val months: List<MonthlyTrendDto>
)

/**
 * Monthly trend DTO
 */
data class MonthlyTrendDto(
    @SerializedName("month")
    val month: String, // YYYY-MM-DD (first day of month)
    @SerializedName("totalIncome")
    val totalIncome: String,
    @SerializedName("totalExpenses")
    val totalExpenses: String,
    @SerializedName("netBalance")
    val netBalance: String
)
