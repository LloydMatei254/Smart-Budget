package com.example.smartbudget.data.repository

import com.example.smartbudget.data.local.dao.CategoryDao
import com.example.smartbudget.data.local.dao.ExpenseDao
import com.example.smartbudget.data.local.dao.IncomeDao
import com.example.smartbudget.data.local.dao.PaymentMethodDao
import com.example.smartbudget.data.mapper.CategoryMapper.toEntity
import com.example.smartbudget.data.mapper.ExpenseMapper.toEntity
import com.example.smartbudget.data.mapper.IncomeMapper.toEntity
import com.example.smartbudget.data.mapper.PaymentMethodMapper.toEntity
import com.example.smartbudget.data.remote.api.SyncApi
import com.example.smartbudget.di.IoDispatcher
import com.example.smartbudget.domain.repository.SyncRepository
import com.example.smartbudget.utils.NetworkUtils
import com.example.smartbudget.utils.PreferencesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SyncRepository for managing data synchronization
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncApi: SyncApi,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val categoryDao: CategoryDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val preferencesManager: PreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SyncRepository {
    
    override suspend fun performFullSync(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            Timber.d("Starting full sync")
            
            // Pull all data from server
            val result = NetworkUtils.safeApiCall {
                syncApi.fullSync(
                    com.example.smartbudget.data.remote.dto.request.SyncRequest(
                        lastSyncTime = null,
                        deviceId = getDeviceId(),
                        appVersion = getAppVersion()
                    )
                )
            }
            
            result.onSuccess { syncResponse ->
                // Update local database with server data
                syncResponse.expenses?.let { expenseData ->
                    val entities = expenseData.created.map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    expenseDao.insertExpenses(entities)
                    Timber.d("Synced ${entities.size} expenses")
                }
                
                syncResponse.income?.let { incomeData ->
                    val entities = incomeData.created.map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    incomeDao.insertIncomes(entities)
                    Timber.d("Synced ${entities.size} income records")
                }
                
                syncResponse.categories?.let { categoryData ->
                    val entities = categoryData.created.map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    categoryDao.insertCategories(entities)
                    Timber.d("Synced ${entities.size} categories")
                }
                
                syncResponse.paymentMethods?.let { paymentMethodData ->
                    val entities = paymentMethodData.created.map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    paymentMethodDao.insertPaymentMethods(entities)
                    Timber.d("Synced ${entities.size} payment methods")
                }
                
                // Update last sync timestamp
                updateLastSyncTime(Instant.parse(syncResponse.syncTime))
                
                Timber.i("Full sync completed successfully")
            }
            
            result.map { Unit }
        } catch (e: Exception) {
            Timber.e(e, "Full sync failed")
            Result.failure(e)
        }
    }
    
    override suspend fun performIncrementalSync(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val lastSyncTime = getLastSyncTime()
            if (lastSyncTime == null) {
                Timber.d("No last sync time, performing full sync")
                return@withContext performFullSync()
            }
            
            Timber.d("Starting incremental sync from $lastSyncTime")
            
            // Get changes since last sync
            val result = NetworkUtils.safeApiCall {
                syncApi.getChanges(
                    lastSyncTime = lastSyncTime.toString(),
                    entityTypes = listOf("expenses", "income", "categories", "payment_methods")
                )
            }
            
            result.onSuccess { syncResponse ->
                // Process created/updated records
                syncResponse.expenses?.let { expenseData ->
                    val createdEntities = expenseData.created.map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    val updatedEntities = expenseData.updated.map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    expenseDao.insertExpenses(createdEntities + updatedEntities)
                    
                    // Handle deletions
                    expenseData.deleted.forEach { id ->
                        expenseDao.softDeleteExpense(id, Instant.now().toEpochMilli())
                    }
                    
                    Timber.d("Incremental sync: ${createdEntities.size} created, ${updatedEntities.size} updated, ${expenseData.deleted.size} deleted expenses")
                }
                
                // Similar processing for income, categories, payment methods
                syncResponse.income?.let { incomeData ->
                    val allEntities = (incomeData.created + incomeData.updated).map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    incomeDao.insertIncomes(allEntities)
                    incomeData.deleted.forEach { id ->
                        incomeDao.softDeleteIncome(id, Instant.now().toEpochMilli())
                    }
                }
                
                syncResponse.categories?.let { categoryData ->
                    val allEntities = (categoryData.created + categoryData.updated).map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    categoryDao.insertCategories(allEntities)
                    categoryData.deleted.forEach { id ->
                        categoryDao.softDeleteCategory(id, Instant.now().toEpochMilli())
                    }
                }
                
                syncResponse.paymentMethods?.let { paymentMethodData ->
                    val allEntities = (paymentMethodData.created + paymentMethodData.updated).map { dto ->
                        dto.toEntity(getCurrentUserId())
                    }
                    paymentMethodDao.insertPaymentMethods(allEntities)
                }
                
                // Handle conflicts
                if (!syncResponse.conflicts.isNullOrEmpty()) {
                    Timber.w("Sync conflicts detected: ${syncResponse.conflicts.size}")
                    // Conflicts would be handled by showing user a resolution UI
                }
                
                // Update last sync timestamp
                updateLastSyncTime(Instant.parse(syncResponse.syncTime))
                
                Timber.i("Incremental sync completed successfully")
            }
            
            result.map { Unit }
        } catch (e: Exception) {
            Timber.e(e, "Incremental sync failed")
            Result.failure(e)
        }
    }
    
    override suspend fun pushPendingChanges(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val pendingCount = getPendingChangesCount()
            if (pendingCount == 0) {
                Timber.d("No pending changes to push")
                return@withContext Result.success(Unit)
            }
            
            Timber.d("Pushing $pendingCount pending changes")
            
            // Get pending changes from each DAO
            val pendingExpenses = expenseDao.getPendingSyncExpenses(getCurrentUserId())
            val pendingIncome = incomeDao.getPendingSyncIncome(getCurrentUserId())
            val pendingCategories = categoryDao.getPendingSyncCategories(getCurrentUserId())
            
            // Convert to sync request format
            val expenseChanges = pendingExpenses.map { entity ->
                com.example.smartbudget.data.remote.dto.request.ExpenseChange(
                    localId = entity.localId ?: entity.id,
                    serverId = if (entity.id.isNotEmpty()) entity.id else null,
                    operation = if (entity.id.isEmpty()) "create" else "update",
                    data = com.example.smartbudget.data.remote.dto.request.CreateExpenseRequest(
                        categoryId = entity.categoryId,
                        paymentMethodId = entity.paymentMethodId,
                        amount = entity.amount,
                        description = entity.description,
                        notes = entity.notes,
                        date = entity.date,
                        localId = entity.localId ?: entity.id
                    ),
                    version = entity.syncVersion,
                    timestamp = Instant.ofEpochMilli(entity.updatedAt).toString()
                )
            }
            
            val incomeChanges = pendingIncome.map { entity ->
                com.example.smartbudget.data.remote.dto.request.IncomeChange(
                    localId = entity.localId ?: entity.id,
                    serverId = if (entity.id.isNotEmpty()) entity.id else null,
                    operation = if (entity.id.isEmpty()) "create" else "update",
                    data = com.example.smartbudget.data.remote.dto.request.CreateIncomeRequest(
                        amount = entity.amount,
                        source = entity.source,
                        description = entity.description,
                        notes = entity.notes,
                        date = entity.date,
                        localId = entity.localId ?: entity.id
                    ),
                    version = entity.syncVersion,
                    timestamp = Instant.ofEpochMilli(entity.updatedAt).toString()
                )
            }
            
            val categoryChanges = pendingCategories.map { entity ->
                com.example.smartbudget.data.remote.dto.request.CategoryChange(
                    localId = entity.localId ?: entity.id,
                    serverId = if (entity.id.isNotEmpty()) entity.id else null,
                    operation = if (entity.id.isEmpty()) "create" else "update",
                    data = com.example.smartbudget.data.remote.dto.request.CreateCategoryRequest(
                        name = entity.name,
                        color = entity.color,
                        icon = entity.icon
                    ),
                    version = 1,
                    timestamp = Instant.ofEpochMilli(entity.updatedAt).toString()
                )
            }
            
            val syncRequest = com.example.smartbudget.data.remote.dto.request.SyncRequest(
                lastSyncTime = getLastSyncTime()?.toString(),
                expenses = expenseChanges.ifEmpty { null },
                income = incomeChanges.ifEmpty { null },
                categories = categoryChanges.ifEmpty { null },
                deviceId = getDeviceId(),
                appVersion = getAppVersion()
            )
            
            // Push changes to server
            val result = NetworkUtils.safeApiCall {
                syncApi.pushChanges(syncRequest)
            }
            
            result.onSuccess { syncResponse ->
                // Update local records with server IDs and mark as synced
                syncResponse.expenses?.created?.forEach { dto ->
                    // Find local record by localId and update with server ID
                    pendingExpenses.find { it.localId == dto.id }?.let { entity ->
                        val updatedEntity = entity.copy(
                            id = dto.id,
                            syncStatus = "synced",
                            syncVersion = dto.syncVersion
                        )
                        expenseDao.updateExpense(updatedEntity)
                    }
                }
                
                Timber.i("Pushed $pendingCount changes successfully")
            }
            
            result.map { Unit }
        } catch (e: Exception) {
            Timber.e(e, "Push pending changes failed")
            Result.failure(e)
        }
    }
    
    override suspend fun getLastSyncTime(): Instant? = withContext(ioDispatcher) {
        try {
            val timestamp = preferencesManager.getLastSyncTimestamp().first()
            timestamp?.let { Instant.ofEpochMilli(it) }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get last sync time")
            null
        }
    }
    
    override suspend fun updateLastSyncTime(timestamp: Instant) {
        preferencesManager.saveLastSyncTimestamp(timestamp.toEpochMilli())
        Timber.d("Updated last sync time: $timestamp")
    }
    
    override suspend fun getPendingChangesCount(): Int = withContext(ioDispatcher) {
        val userId = getCurrentUserId()
        val expenseCount = expenseDao.getPendingSyncExpenses(userId).size
        val incomeCount = incomeDao.getPendingSyncIncome(userId).size
        val categoryCount = categoryDao.getPendingSyncCategories(userId).size
        expenseCount + incomeCount + categoryCount
    }
    
    override suspend fun isSyncNeeded(): Boolean {
        return getPendingChangesCount() > 0
    }
    
    private fun getCurrentUserId(): String {
        return runBlocking {
            preferencesManager.getUserIdSync() ?: ""
        }
    }
    
    private fun getDeviceId(): String {
        // In production, this would use AndroidId or a generated UUID stored in preferences
        return "device_${System.currentTimeMillis()}"
    }
    
    private fun getAppVersion(): String {
        return "1.0.0" // Would come from BuildConfig.VERSION_NAME
    }
}
