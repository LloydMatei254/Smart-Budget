package com.example.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartbudget.data.local.entities.IncomeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Income table
 */
@Dao
interface IncomeDao {
    
    /**
     * Insert or replace income
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)
    
    /**
     * Insert or replace multiple income records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomes(incomes: List<IncomeEntity>)
    
    /**
     * Update income
     */
    @Update
    suspend fun updateIncome(income: IncomeEntity)
    
    /**
     * Get all income for user
     */
    @Query("SELECT * FROM income WHERE userId = :userId AND deletedAt IS NULL ORDER BY date DESC, createdAt DESC")
    suspend fun getIncomeForUser(userId: String): List<IncomeEntity>
    
    /**
     * Get all income for user as Flow
     */
    @Query("SELECT * FROM income WHERE userId = :userId AND deletedAt IS NULL ORDER BY date DESC, createdAt DESC")
    fun getIncomeForUserFlow(userId: String): Flow<List<IncomeEntity>>
    
    /**
     * Get income for user with date range
     */
    @Query("""
        SELECT * FROM income 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    suspend fun getIncomeForUserInDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): List<IncomeEntity>
    
    /**
     * Get income for user with date range as Flow
     */
    @Query("""
        SELECT * FROM income 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getIncomeForUserInDateRangeFlow(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<IncomeEntity>>
    
    /**
     * Get income by source
     */
    @Query("""
        SELECT * FROM income 
        WHERE userId = :userId 
        AND source = :source 
        AND deletedAt IS NULL 
        ORDER BY date DESC
    """)
    suspend fun getIncomeBySource(userId: String, source: String): List<IncomeEntity>
    
    /**
     * Get income by ID
     */
    @Query("SELECT * FROM income WHERE id = :incomeId AND deletedAt IS NULL LIMIT 1")
    suspend fun getIncomeById(incomeId: String): IncomeEntity?
    
    /**
     * Get income by ID as Flow
     */
    @Query("SELECT * FROM income WHERE id = :incomeId AND deletedAt IS NULL LIMIT 1")
    fun getIncomeByIdFlow(incomeId: String): Flow<IncomeEntity?>
    
    /**
     * Get income by local ID (for sync matching)
     */
    @Query("SELECT * FROM income WHERE localId = :localId LIMIT 1")
    suspend fun getIncomeByLocalId(localId: String): IncomeEntity?
    
    /**
     * Get pending sync income
     */
    @Query("SELECT * FROM income WHERE userId = :userId AND syncStatus = 'pending'")
    suspend fun getPendingSyncIncome(userId: String): List<IncomeEntity>
    
    /**
     * Get total income amount for user in date range
     */
    @Query("""
        SELECT COALESCE(SUM(CAST(amount AS REAL)), 0) 
        FROM income 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    suspend fun getTotalIncomeAmount(
        userId: String,
        startDate: String,
        endDate: String
    ): Double
    
    /**
     * Get income count for user
     */
    @Query("SELECT COUNT(*) FROM income WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun getIncomeCount(userId: String): Int
    
    /**
     * Soft delete income by ID
     */
    @Query("UPDATE income SET deletedAt = :timestamp, syncStatus = 'deleted' WHERE id = :incomeId")
    suspend fun softDeleteIncome(incomeId: String, timestamp: Long)
    
    /**
     * Hard delete income by ID
     */
    @Query("DELETE FROM income WHERE id = :incomeId")
    suspend fun deleteIncome(incomeId: String)
    
    /**
     * Delete all income for user
     */
    @Query("DELETE FROM income WHERE userId = :userId")
    suspend fun deleteAllIncomeForUser(userId: String)
    
    /**
     * Delete all income
     */
    @Query("DELETE FROM income")
    suspend fun deleteAllIncome()
}
