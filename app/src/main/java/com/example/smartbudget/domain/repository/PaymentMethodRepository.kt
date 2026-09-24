package com.example.smartbudget.domain.repository

import com.example.smartbudget.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for PaymentMethod operations
 */
interface PaymentMethodRepository {
    
    /**
     * Get all payment methods as Flow
     */
    fun getPaymentMethodsFlow(): Flow<List<PaymentMethod>>
    
    /**
     * Get all payment methods
     */
    suspend fun getPaymentMethods(): List<PaymentMethod>
    
    /**
     * Get payment method by ID
     */
    suspend fun getPaymentMethodById(paymentMethodId: String): PaymentMethod?
    
    /**
     * Get default payment method
     */
    suspend fun getDefaultPaymentMethod(): PaymentMethod?
    
    /**
     * Create new payment method (offline-first)
     */
    suspend fun createPaymentMethod(paymentMethod: PaymentMethod): Result<PaymentMethod>
    
    /**
     * Update payment method (offline-first)
     */
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod): Result<PaymentMethod>
    
    /**
     * Delete payment method
     */
    suspend fun deletePaymentMethod(paymentMethodId: String): Result<Unit>
    
    /**
     * Set payment method as default
     */
    suspend fun setDefaultPaymentMethod(paymentMethodId: String): Result<PaymentMethod>
    
    /**
     * Sync payment methods with server
     */
    suspend fun syncPaymentMethods(): Result<Unit>
    
    /**
     * Force refresh from server
     */
    suspend fun refreshPaymentMethods(): Result<Unit>
}
