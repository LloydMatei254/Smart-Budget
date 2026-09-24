package com.example.smartbudget.domain.repository

import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.domain.model.IncomeSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Income operations
 */
interface IncomeRepository {
    
    /**
     * Get all income as Flow
     */
    fun getIncomeFlow(): Flow<List<Income>>
    
    /**
     * Get income for date range
     */
    suspend fun getIncomeForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Income>
    
    /**
     * Get income for date range as Flow
     */
    fun getIncomeForDateRangeFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Income>>
    
    /**
     * Get income by ID
     */
    suspend fun getIncomeById(incomeId: String): Income?
    
    /**
     * Get income by source
     */
    suspend fun getIncomeBySource(source: IncomeSource): List<Income>
    
    /**
     * Create new income (offline-first)
     */
    suspend fun createIncome(income: Income): Result<Income>
    
    /**
     * Update income (offline-first)
     */
    suspend fun updateIncome(income: Income): Result<Income>
    
    /**
     * Delete income (offline-first soft delete)
     */
    suspend fun deleteIncome(incomeId: String): Result<Unit>
    
    /**
     * Sync income with server
     */
    suspend fun syncIncome(): Result<Unit>
    
    /**
     * Force refresh from server
     */
    suspend fun refreshIncome(): Result<Unit>
}
