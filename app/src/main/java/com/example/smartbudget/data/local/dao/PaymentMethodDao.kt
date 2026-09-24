package com.example.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartbudget.data.local.entities.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Payment Method table
 */
@Dao
interface PaymentMethodDao {
    
    /**
     * Insert or replace payment method
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity)
    
    /**
     * Insert or replace multiple payment methods
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)
    
    /**
     * Update payment method
     */
    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)
    
    /**
     * Get all payment methods for user
     */
    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY name ASC")
    suspend fun getPaymentMethodsForUser(userId: String): List<PaymentMethodEntity>
    
    /**
     * Get all payment methods for user as Flow
     */
    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY name ASC")
    fun getPaymentMethodsForUserFlow(userId: String): Flow<List<PaymentMethodEntity>>
    
    /**
     * Get payment method by ID
     */
    @Query("SELECT * FROM payment_methods WHERE id = :paymentMethodId LIMIT 1")
    suspend fun getPaymentMethodById(paymentMethodId: String): PaymentMethodEntity?
    
    /**
     * Get payment method by ID as Flow
     */
    @Query("SELECT * FROM payment_methods WHERE id = :paymentMethodId LIMIT 1")
    fun getPaymentMethodByIdFlow(paymentMethodId: String): Flow<PaymentMethodEntity?>
    
    /**
     * Get default payment method for user
     */
    @Query("SELECT * FROM payment_methods WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultPaymentMethod(userId: String): PaymentMethodEntity?
    
    /**
     * Delete payment method by ID
     */
    @Query("DELETE FROM payment_methods WHERE id = :paymentMethodId")
    suspend fun deletePaymentMethod(paymentMethodId: String)
    
    /**
     * Delete all payment methods for user
     */
    @Query("DELETE FROM payment_methods WHERE userId = :userId")
    suspend fun deleteAllPaymentMethodsForUser(userId: String)
    
    /**
     * Delete all payment methods
     */
    @Query("DELETE FROM payment_methods")
    suspend fun deleteAllPaymentMethods()
    
    /**
     * Clear default flag from all payment methods for user
     */
    @Query("UPDATE payment_methods SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultPaymentMethods(userId: String)
    
    /**
     * Set payment method as default
     */
    @Query("UPDATE payment_methods SET isDefault = 1 WHERE id = :paymentMethodId")
    suspend fun setDefaultPaymentMethod(paymentMethodId: String)
}
