package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.PaginatedResponse
import com.example.smartbudget.data.remote.dto.request.CreateExpenseRequest
import com.example.smartbudget.data.remote.dto.request.UpdateExpenseRequest
import com.example.smartbudget.data.remote.dto.response.ExpenseDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Expense endpoints
 */
interface ExpensesApi {
    
    /**
     * Get all expenses for current user with pagination
     */
    @GET("expenses")
    suspend fun getExpenses(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("paymentMethodId") paymentMethodId: String? = null,
        @Query("sortBy") sortBy: String = "date",
        @Query("sortOrder") sortOrder: String = "desc"
    ): Response<ApiResponse<PaginatedResponse<ExpenseDto>>>
    
    /**
     * Get expense by ID
     */
    @GET("expenses/{id}")
    suspend fun getExpenseById(
        @Path("id") expenseId: String
    ): Response<ApiResponse<ExpenseDto>>
    
    /**
     * Create new expense
     */
    @POST("expenses")
    suspend fun createExpense(
        @Body request: CreateExpenseRequest
    ): Response<ApiResponse<ExpenseDto>>
    
    /**
     * Update existing expense
     */
    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") expenseId: String,
        @Body request: UpdateExpenseRequest
    ): Response<ApiResponse<ExpenseDto>>
    
    /**
     * Delete expense (soft delete)
     */
    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") expenseId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Bulk create expenses
     */
    @POST("expenses/bulk")
    suspend fun bulkCreateExpenses(
        @Body requests: List<CreateExpenseRequest>
    ): Response<ApiResponse<List<ExpenseDto>>>
    
    /**
     * Get total expenses for date range
     */
    @GET("expenses/total")
    suspend fun getTotalExpenses(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("categoryId") categoryId: String? = null
    ): Response<ApiResponse<Map<String, Any>>>
}
