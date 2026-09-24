package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.request.CreateCategoryRequest
import com.example.smartbudget.data.remote.dto.request.UpdateCategoryRequest
import com.example.smartbudget.data.remote.dto.response.CategoryDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Category endpoints
 */
interface CategoriesApi {
    
    /**
     * Get all categories for current user
     */
    @GET("categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>
    
    /**
     * Get category by ID
     */
    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") categoryId: String
    ): Response<ApiResponse<CategoryDto>>
    
    /**
     * Create new category
     */
    @POST("categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): Response<ApiResponse<CategoryDto>>
    
    /**
     * Update existing category
     */
    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") categoryId: String,
        @Body request: UpdateCategoryRequest
    ): Response<ApiResponse<CategoryDto>>
    
    /**
     * Delete category (soft delete)
     */
    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") categoryId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Get default categories
     */
    @GET("categories/default")
    suspend fun getDefaultCategories(): Response<ApiResponse<List<CategoryDto>>>
}
