package com.example.smartbudget.data.repository

import com.example.smartbudget.data.local.dao.ExpenseDao
import com.example.smartbudget.data.mapper.ExpenseMapper.toDomain
import com.example.smartbudget.data.mapper.ExpenseMapper.toEntity
import com.example.smartbudget.data.remote.api.ExpensesApi
import com.example.smartbudget.data.remote.dto.request.CreateExpenseRequest
import com.example.smartbudget.data.remote.dto.request.UpdateExpenseRequest
import com.example.smartbudget.di.IoDispatcher
import com.example.smartbudget.domain.model.Expense
import com.example.smartbudget.domain.model.ExpenseWithDetails
import com.example.smartbudget.domain.model.SyncStatus
import com.example.smartbudget.domain.repository.ExpenseRepository
import com.example.smartbudget.utils.NetworkUtils
import com.example.smartbudget.utils.PreferencesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExpenseRepository with offline-first approach
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val expensesApi: ExpensesApi,
    private val preferencesManager: PreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ExpenseRepository {
    
    override fun getExpensesFlow(): Flow<List<Expense>> {
        return expenseDao.getExpensesForUserFlow(getCurrentUserId())
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override fun getExpensesWithDetailsFlow(): Flow<List<ExpenseWithDetails>> {
        return expenseDao.getExpensesWithRelationsFlow(getCurrentUserId())
            .map { relations -> relations.map { it.toDomain() } }
    }
    
    override suspend fun getExpensesForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Expense> = withContext(ioDispatcher) {
        expenseDao.getExpensesForUserInDateRange(
            userId = getCurrentUserId(),
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { it.toDomain() }
    }
    
    override fun getExpensesForDateRangeFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>> {
        return expenseDao.getExpensesForUserInDateRangeFlow(
            userId = getCurrentUserId(),
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getExpenseById(expenseId: String): Expense? = 
        withContext(ioDispatcher) {
            expenseDao.getExpenseById(expenseId)?.toDomain()
        }
    
    override suspend fun getExpenseWithDetailsById(expenseId: String): ExpenseWithDetails? =
        withContext(ioDispatcher) {
            expenseDao.getExpenseWithRelationsById(expenseId)?.toDomain()
        }
    
    override suspend fun getExpensesByCategory(categoryId: String): List<Expense> =
        withContext(ioDispatcher) {
            expenseDao.getExpensesByCategory(getCurrentUserId(), categoryId)
                .map { it.toDomain() }
        }
    
    override suspend fun createExpense(expense: Expense): Result<Expense> =
        withContext(ioDispatcher) {
            try {
                // Generate local ID for offline tracking
                val localId = UUID.randomUUID().toString()
                val expenseWithLocal = expense.copy(
                    localId = localId,
                    syncStatus = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                // Save to local database first (offline-first)
                expenseDao.insertExpense(expenseWithLocal.toEntity())
                Timber.d("Expense saved locally with localId: $localId")
                
                // Try to sync with server if online
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        val request = CreateExpenseRequest(
                            categoryId = expense.categoryId,
                            paymentMethodId = expense.paymentMethodId,
                            amount = expense.amount.toPlainString(),
                            description = expense.description,
                            notes = expense.notes,
                            date = expense.date.toString(),
                            localId = localId
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            expensesApi.createExpense(request)
                        }
                        
                        result.onSuccess { serverExpense ->
                            // Update local record with server ID and mark as synced
                            val syncedExpense = expenseWithLocal.copy(
                                id = serverExpense.id,
                                syncStatus = SyncStatus.SYNCED,
                                syncVersion = serverExpense.syncVersion,
                                localId = localId,
                                receiptPhotoUrl = expenseWithLocal.receiptPhotoUrl
                            )
                            expenseDao.insertExpense(syncedExpense.toEntity())
                            Timber.d("Expense synced with server: ${serverExpense.id}")
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync expense immediately, will sync later")
                    }
                }
                
                Result.success(expenseWithLocal)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create expense")
                Result.failure(e)
            }
        }
    
    override suspend fun updateExpense(expense: Expense): Result<Expense> =
        withContext(ioDispatcher) {
            try {
                // Mark as pending sync
                val updatedExpense = expense.copy(
                    syncStatus = SyncStatus.PENDING,
                    updatedAt = Instant.now()
                ).incrementVersion()
                
                // Update local database first
                expenseDao.updateExpense(updatedExpense.toEntity())
                Timber.d("Expense updated locally: ${expense.id}")
                
                // Try to sync with server if online
                if (NetworkUtils.isNetworkAvailable() && expense.id.isNotEmpty()) {
                    try {
                        val request = UpdateExpenseRequest(
                            categoryId = expense.categoryId,
                            paymentMethodId = expense.paymentMethodId,
                            amount = expense.amount.toPlainString(),
                            description = expense.description,
                            notes = expense.notes,
                            date = expense.date.toString(),
                            syncVersion = updatedExpense.syncVersion
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            expensesApi.updateExpense(expense.id, request)
                        }
                        
                        result.onSuccess { serverExpense ->
                            val syncedExpense = updatedExpense.copy(
                                syncStatus = SyncStatus.SYNCED,
                                syncVersion = serverExpense.syncVersion,
                                localId = updatedExpense.localId,
                                receiptPhotoUrl = updatedExpense.receiptPhotoUrl
                            )
                            expenseDao.updateExpense(syncedExpense.toEntity())
                            Timber.d("Expense synced with server: ${serverExpense.id}")
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync expense update, will sync later")
                    }
                }
                
                Result.success(updatedExpense)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update expense")
                Result.failure(e)
            }
        }
    
    override suspend fun deleteExpense(expenseId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                // Soft delete locally
                expenseDao.softDeleteExpense(expenseId, Instant.now().toEpochMilli())
                Timber.d("Expense soft deleted locally: $expenseId")
                
                // Try to delete on server if online
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        NetworkUtils.safeApiCall {
                            expensesApi.deleteExpense(expenseId)
                        }
                        Timber.d("Expense deleted on server: $expenseId")
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to delete on server, will sync later")
                    }
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete expense")
                Result.failure(e)
            }
        }
    
    override suspend fun syncExpenses(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            // Get pending expenses
            val pendingExpenses = expenseDao.getPendingSyncExpenses(getCurrentUserId())
            Timber.d("Found ${pendingExpenses.size} pending expenses to sync")
            
            // Sync each pending expense
            pendingExpenses.forEach { entity ->
                val expense = entity.toDomain()
                if (expense.id.isEmpty()) {
                    // Create on server
                    createExpense(expense)
                } else {
                    // Update on server
                    updateExpense(expense)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            Result.failure(e)
        }
    }
    
    override suspend fun refreshExpenses(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                expensesApi.getExpenses(page = 1, limit = 100)
            }
            
            result.onSuccess { paginatedResponse ->
                // Update local database with server data
                val entities = paginatedResponse.items.map { dto ->
                    dto.toEntity(getCurrentUserId())
                }
                expenseDao.insertExpenses(entities)
                Timber.d("Refreshed ${entities.size} expenses from server")
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
