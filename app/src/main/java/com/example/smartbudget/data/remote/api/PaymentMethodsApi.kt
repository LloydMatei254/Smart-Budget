package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.request.CreatePaymentMethodRequest
import com.example.smartbudget.data.remote.dto.request.UpdatePaymentMethodRequest
import com.example.smartbudget.data.remote.dto.response.PaymentMethodDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Payment Method endpoints
 */
interface PaymentMethodsApi {
    
    /**
     * Get all payment methods for current user
     */
    @GET("payment-methods")
    suspend fun getPaymentMethods(): Response<ApiResponse<List<PaymentMethodDto>>>
    
    /**
     * Get payment method by ID
     */
    @GET("payment-methods/{id}")
    suspend fun getPaymentMethodById(
        @Path("id") paymentMethodId: String
    ): Response<ApiResponse<PaymentMethodDto>>
    
    /**
     * Create new payment method
     */
    @POST("payment-methods")
    suspend fun createPaymentMethod(
        @Body request: CreatePaymentMethodRequest
    ): Response<ApiResponse<PaymentMethodDto>>
    
    /**
     * Update existing payment method
     */
    @PUT("payment-methods/{id}")
    suspend fun updatePaymentMethod(
        @Path("id") paymentMethodId: String,
        @Body request: UpdatePaymentMethodRequest
    ): Response<ApiResponse<PaymentMethodDto>>
    
    /**
     * Delete payment method
     */
    @DELETE("payment-methods/{id}")
    suspend fun deletePaymentMethod(
        @Path("id") paymentMethodId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Set default payment method
     */
    @PUT("payment-methods/{id}/set-default")
    suspend fun setDefaultPaymentMethod(
        @Path("id") paymentMethodId: String
    ): Response<ApiResponse<PaymentMethodDto>>
}
