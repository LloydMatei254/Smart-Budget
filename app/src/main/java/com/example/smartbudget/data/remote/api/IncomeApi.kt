package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.PaginatedResponse
import com.example.smartbudget.data.remote.dto.request.CreateIncomeRequest
import com.example.smartbudget.data.remote.dto.request.UpdateIncomeRequest
import com.example.smartbudget.data.remote.dto.response.IncomeDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Income endpoints
 */
interface IncomeApi {
    
    /**
     * Get all income for current user with pagination
     */
    @GET("income")
    suspend fun getIncome(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("source") source: String? = null,
        @Query("sortBy") sortBy: String = "date",
        @Query("sortOrder") sortOrder: String = "desc"
    ): Response<ApiResponse<PaginatedResponse<IncomeDto>>>
    
    /**
     * Get income by ID
     */
    @GET("income/{id}")
    suspend fun getIncomeById(
        @Path("id") incomeId: String
    ): Response<ApiResponse<IncomeDto>>
    
    /**
     * Create new income
     */
    @POST("income")
    suspend fun createIncome(
        @Body request: CreateIncomeRequest
    ): Response<ApiResponse<IncomeDto>>
    
    /**
     * Update existing income
     */
    @PUT("income/{id}")
    suspend fun updateIncome(
        @Path("id") incomeId: String,
        @Body request: UpdateIncomeRequest
    ): Response<ApiResponse<IncomeDto>>
    
    /**
     * Delete income (soft delete)
     */
    @DELETE("income/{id}")
    suspend fun deleteIncome(
        @Path("id") incomeId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Bulk create income
     */
    @POST("income/bulk")
    suspend fun bulkCreateIncome(
        @Body requests: List<CreateIncomeRequest>
    ): Response<ApiResponse<List<IncomeDto>>>
    
    /**
     * Get total income for date range
     */
    @GET("income/total")
    suspend fun getTotalIncome(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("source") source: String? = null
    ): Response<ApiResponse<Map<String, Any>>>
}
