package com.example.smartbudget.data.repository

import com.example.smartbudget.data.local.dao.IncomeDao
import com.example.smartbudget.data.mapper.IncomeMapper.toDomain
import com.example.smartbudget.data.mapper.IncomeMapper.toEntity
import com.example.smartbudget.data.remote.api.IncomeApi
import com.example.smartbudget.data.remote.dto.request.CreateIncomeRequest
import com.example.smartbudget.data.remote.dto.request.UpdateIncomeRequest
import com.example.smartbudget.di.IoDispatcher
import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.domain.model.IncomeSource
import com.example.smartbudget.domain.model.SyncStatus
import com.example.smartbudget.domain.repository.IncomeRepository
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

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao,
    private val incomeApi: IncomeApi,
    private val preferencesManager: PreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IncomeRepository {
    
    override fun getIncomeFlow(): Flow<List<Income>> {
        return incomeDao.getIncomeForUserFlow(getCurrentUserId())
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getIncomeForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Income> = withContext(ioDispatcher) {
        incomeDao.getIncomeForUserInDateRange(
            userId = getCurrentUserId(),
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { it.toDomain() }
    }
    
    override fun getIncomeForDateRangeFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Income>> {
        return incomeDao.getIncomeForUserInDateRangeFlow(
            userId = getCurrentUserId(),
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getIncomeById(incomeId: String): Income? =
        withContext(ioDispatcher) {
            incomeDao.getIncomeById(incomeId)?.toDomain()
        }
    
    override suspend fun getIncomeBySource(source: IncomeSource): List<Income> =
        withContext(ioDispatcher) {
            incomeDao.getIncomeBySource(getCurrentUserId(), source.name)
                .map { it.toDomain() }
        }
    
    override suspend fun createIncome(income: Income): Result<Income> =
        withContext(ioDispatcher) {
            try {
                val localId = UUID.randomUUID().toString()
                val incomeWithLocal = income.copy(
                    localId = localId,
                    syncStatus = SyncStatus.PENDING,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                
                incomeDao.insertIncome(incomeWithLocal.toEntity())
                Timber.d("Income saved locally: $localId")
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        val request = CreateIncomeRequest(
                            amount = income.amount.toPlainString(),
                            source = income.source.name,
                            description = income.description,
                            notes = income.notes,
                            date = income.date.toString(),
                            localId = localId
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            incomeApi.createIncome(request)
                        }
                        
                        result.onSuccess { serverIncome ->
                            val syncedIncome = incomeWithLocal.copy(
                                id = serverIncome.id,
                                syncStatus = SyncStatus.SYNCED,
                                syncVersion = serverIncome.syncVersion,
                                localId = localId,
                                isRecurring = incomeWithLocal.isRecurring
                            )
                            incomeDao.insertIncome(syncedIncome.toEntity())
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync income")
                    }
                }
                
                Result.success(incomeWithLocal)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create income")
                Result.failure(e)
            }
        }
    
    override suspend fun updateIncome(income: Income): Result<Income> =
        withContext(ioDispatcher) {
            try {
                val updatedIncome = income.copy(
                    syncStatus = SyncStatus.PENDING,
                    updatedAt = Instant.now()
                ).incrementVersion()
                
                incomeDao.updateIncome(updatedIncome.toEntity())
                
                if (NetworkUtils.isNetworkAvailable() && income.id.isNotEmpty()) {
                    try {
                        val request = UpdateIncomeRequest(
                            amount = income.amount.toPlainString(),
                            source = income.source.name,
                            description = income.description,
                            notes = income.notes,
                            date = income.date.toString(),
                            syncVersion = updatedIncome.syncVersion
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            incomeApi.updateIncome(income.id, request)
                        }
                        
                        result.onSuccess { serverIncome ->
                            val syncedIncome = updatedIncome.copy(
                                syncStatus = SyncStatus.SYNCED,
                                syncVersion = serverIncome.syncVersion,
                                localId = updatedIncome.localId,
                                isRecurring = updatedIncome.isRecurring
                            )
                            incomeDao.updateIncome(syncedIncome.toEntity())
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync income update")
                    }
                }
                
                Result.success(updatedIncome)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update income")
                Result.failure(e)
            }
        }
    
    override suspend fun deleteIncome(incomeId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                incomeDao.softDeleteIncome(incomeId, Instant.now().toEpochMilli())
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        NetworkUtils.safeApiCall {
                            incomeApi.deleteIncome(incomeId)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to delete income on server")
                    }
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete income")
                Result.failure(e)
            }
        }
    
    override suspend fun syncIncome(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val pendingIncome = incomeDao.getPendingSyncIncome(getCurrentUserId())
            
            pendingIncome.forEach { entity ->
                val income = entity.toDomain()
                if (income.id.isEmpty()) {
                    createIncome(income)
                } else {
                    updateIncome(income)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            Result.failure(e)
        }
    }
    
    override suspend fun refreshIncome(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                incomeApi.getIncome(page = 1, limit = 100)
            }
            
            result.onSuccess { paginatedResponse ->
                val entities = paginatedResponse.items.map { dto ->
                    dto.toEntity(getCurrentUserId())
                }
                incomeDao.insertIncomes(entities)
                Timber.d("Refreshed ${entities.size} income records")
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
