package com.example.smartbudget.data.repository

import com.example.smartbudget.data.local.dao.PaymentMethodDao
import com.example.smartbudget.data.mapper.PaymentMethodMapper.toDomain
import com.example.smartbudget.data.mapper.PaymentMethodMapper.toEntity
import com.example.smartbudget.data.remote.api.PaymentMethodsApi
import com.example.smartbudget.data.remote.dto.request.CreatePaymentMethodRequest
import com.example.smartbudget.data.remote.dto.request.UpdatePaymentMethodRequest
import com.example.smartbudget.di.IoDispatcher
import com.example.smartbudget.domain.model.PaymentMethod
import com.example.smartbudget.domain.repository.PaymentMethodRepository
import com.example.smartbudget.utils.NetworkUtils
import com.example.smartbudget.utils.PreferencesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMethodRepositoryImpl @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao,
    private val paymentMethodsApi: PaymentMethodsApi,
    private val preferencesManager: PreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PaymentMethodRepository {
    
    override fun getPaymentMethodsFlow(): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getPaymentMethodsForUserFlow(getCurrentUserId())
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getPaymentMethods(): List<PaymentMethod> =
        withContext(ioDispatcher) {
            paymentMethodDao.getPaymentMethodsForUser(getCurrentUserId())
                .map { it.toDomain() }
        }
    
    override suspend fun getPaymentMethodById(paymentMethodId: String): PaymentMethod? =
        withContext(ioDispatcher) {
            paymentMethodDao.getPaymentMethodById(paymentMethodId)?.toDomain()
        }
    
    override suspend fun getDefaultPaymentMethod(): PaymentMethod? =
        withContext(ioDispatcher) {
            paymentMethodDao.getDefaultPaymentMethod(getCurrentUserId())?.toDomain()
        }
    
    override suspend fun createPaymentMethod(paymentMethod: PaymentMethod): Result<PaymentMethod> =
        withContext(ioDispatcher) {
            try {
                val newPaymentMethod = paymentMethod.copy(
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                paymentMethodDao.insertPaymentMethod(newPaymentMethod.toEntity())
                Timber.d("Payment method saved locally: ${paymentMethod.name}")
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        val request = CreatePaymentMethodRequest(
                            name = paymentMethod.name,
                            isDefault = paymentMethod.isDefault
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            paymentMethodsApi.createPaymentMethod(request)
                        }
                        
                        result.onSuccess { serverPaymentMethod ->
                            val syncedPaymentMethod = newPaymentMethod.copy(
                                id = serverPaymentMethod.id
                            )
                            paymentMethodDao.insertPaymentMethod(syncedPaymentMethod.toEntity())
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync payment method")
                    }
                }
                
                Result.success(newPaymentMethod)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create payment method")
                Result.failure(e)
            }
        }
    
    override suspend fun updatePaymentMethod(paymentMethod: PaymentMethod): Result<PaymentMethod> =
        withContext(ioDispatcher) {
            try {
                val updatedPaymentMethod = paymentMethod.copy(
                    updatedAt = Instant.now()
                )
                
                paymentMethodDao.updatePaymentMethod(updatedPaymentMethod.toEntity())
                
                if (NetworkUtils.isNetworkAvailable() && paymentMethod.id.isNotEmpty()) {
                    try {
                        val request = UpdatePaymentMethodRequest(
                            name = paymentMethod.name,
                            isDefault = paymentMethod.isDefault
                        )
                        
                        NetworkUtils.safeApiCall {
                            paymentMethodsApi.updatePaymentMethod(paymentMethod.id, request)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync payment method update")
                    }
                }
                
                Result.success(updatedPaymentMethod)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update payment method")
                Result.failure(e)
            }
        }
    
    override suspend fun deletePaymentMethod(paymentMethodId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                paymentMethodDao.deletePaymentMethod(paymentMethodId)
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        NetworkUtils.safeApiCall {
                            paymentMethodsApi.deletePaymentMethod(paymentMethodId)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to delete payment method on server")
                    }
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete payment method")
                Result.failure(e)
            }
        }
    
    override suspend fun setDefaultPaymentMethod(paymentMethodId: String): Result<PaymentMethod> =
        withContext(ioDispatcher) {
            try {
                // Clear other defaults first
                paymentMethodDao.clearDefaultPaymentMethods(getCurrentUserId())
                
                // Set new default locally
                paymentMethodDao.setDefaultPaymentMethod(paymentMethodId)
                
                val paymentMethod = paymentMethodDao.getPaymentMethodById(paymentMethodId)
                    ?: return@withContext Result.failure(Exception("Payment method not found"))
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        val result = NetworkUtils.safeApiCall {
                            paymentMethodsApi.setDefaultPaymentMethod(paymentMethodId)
                        }
                        result.getOrNull()
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to set default on server")
                    }
                }
                
                Result.success(paymentMethod.toDomain())
            } catch (e: Exception) {
                Timber.e(e, "Failed to set default payment method")
                Result.failure(e)
            }
        }
    
    override suspend fun syncPaymentMethods(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            // Refresh from server
            refreshPaymentMethods()
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            Result.failure(e)
        }
    }
    
    override suspend fun refreshPaymentMethods(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                paymentMethodsApi.getPaymentMethods()
            }
            
            result.onSuccess { paymentMethods ->
                val entities = paymentMethods.map { dto ->
                    dto.toEntity(getCurrentUserId())
                }
                paymentMethodDao.insertPaymentMethods(entities)
                Timber.d("Refreshed ${entities.size} payment methods")
            }
            
            result.map { Unit }
        } catch (e: Exception) {
            Timber.e(e, "Refresh failed")
            Result.failure(e)
        }
    }
    
    private fun getCurrentUserId(): String {
        return runBlocking {
            preferencesManager.getUserIdSync() ?: ""
        }
    }
}
