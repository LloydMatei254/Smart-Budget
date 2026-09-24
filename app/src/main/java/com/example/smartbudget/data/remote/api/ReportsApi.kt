package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.response.CategorySpendingDto
import com.example.smartbudget.data.remote.dto.response.FinancialSummaryDto
import com.example.smartbudget.data.remote.dto.response.MonthlyTrendDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for Reports endpoints
 */
interface ReportsApi {
    
    /**
     * Get financial summary for date range
     */
    @GET("reports/summary")
    suspend fun getFinancialSummary(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ApiResponse<FinancialSummaryDto>>
    
    /**
     * Get spending by category
     */
    @GET("reports/spending-by-category")
    suspend fun getSpendingByCategory(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ApiResponse<List<CategorySpendingDto>>>
    
    /**
     * Get monthly trends
     */
    @GET("reports/monthly-trends")
    suspend fun getMonthlyTrends(
        @Query("months") months: Int = 6
    ): Response<ApiResponse<List<MonthlyTrendDto>>>
    
    /**
     * Get income vs expenses comparison
     */
    @GET("reports/income-vs-expenses")
    suspend fun getIncomeVsExpenses(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("groupBy") groupBy: String = "month" // day, week, month, year
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Get expense forecast based on historical data
     */
    @GET("reports/expense-forecast")
    suspend fun getExpenseForecast(
        @Query("months") months: Int = 3
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Get budget adherence report
     */
    @GET("reports/budget-adherence")
    suspend fun getBudgetAdherence(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ApiResponse<Map<String, Any>>>
}
