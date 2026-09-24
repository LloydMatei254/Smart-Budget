package com.example.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.smartbudget.data.local.entities.ExpenseEntity
import com.example.smartbudget.data.local.relations.ExpenseWithRelations
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Expense table
 */
@Dao
interface ExpenseDao {
    
    /**
     * Insert or replace expense
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
    
    /**
     * Insert or replace multiple expenses
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
    
    /**
     * Update expense
     */
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    /**
     * Get all expenses for user
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND deletedAt IS NULL ORDER BY date DESC, createdAt DESC")
    suspend fun getExpensesForUser(userId: String): List<ExpenseEntity>
    
    /**
     * Get all expenses for user as Flow
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND deletedAt IS NULL ORDER BY date DESC, createdAt DESC")
    fun getExpensesForUserFlow(userId: String): Flow<List<ExpenseEntity>>
    
    /**
     * Get expenses for user with date range
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    suspend fun getExpensesForUserInDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): List<ExpenseEntity>
    
    /**
     * Get expenses for user with date range as Flow
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpensesForUserInDateRangeFlow(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<ExpenseEntity>>
    
    /**
     * Get expenses by category
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        AND deletedAt IS NULL 
        ORDER BY date DESC
    """)
    suspend fun getExpensesByCategory(userId: String, categoryId: String): List<ExpenseEntity>
    
    /**
     * Get expense by ID
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId AND deletedAt IS NULL LIMIT 1")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?
    
    /**
     * Get expense by ID as Flow
     */
    @Query("SELECT * FROM expenses WHERE id = :expenseId AND deletedAt IS NULL LIMIT 1")
    fun getExpenseByIdFlow(expenseId: String): Flow<ExpenseEntity?>
    
    /**
     * Get expense by local ID (for sync matching)
     */
    @Query("SELECT * FROM expenses WHERE localId = :localId LIMIT 1")
    suspend fun getExpenseByLocalId(localId: String): ExpenseEntity?
    
    /**
     * Get pending sync expenses
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND syncStatus = 'pending'")
    suspend fun getPendingSyncExpenses(userId: String): List<ExpenseEntity>
    
    /**
     * Get total expense amount for user in date range
     */
    @Query("""
        SELECT COALESCE(SUM(CAST(amount AS REAL)), 0) 
        FROM expenses 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    suspend fun getTotalExpenseAmount(
        userId: String,
        startDate: String,
        endDate: String
    ): Double
    
    /**
     * Get expense count for user
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun getExpenseCount(userId: String): Int
    
    /**
     * Soft delete expense by ID
     */
    @Query("UPDATE expenses SET deletedAt = :timestamp, syncStatus = 'deleted' WHERE id = :expenseId")
    suspend fun softDeleteExpense(expenseId: String, timestamp: Long)
    
    /**
     * Hard delete expense by ID
     */
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)
    
    /**
     * Delete all expenses for user
     */
    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteAllExpensesForUser(userId: String)
    
    /**
     * Delete all expenses
     */
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
    
    /**
     * Get expenses with their related category and payment method
     */
    @Transaction
    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        ORDER BY date DESC, createdAt DESC
    """)
    suspend fun getExpensesWithRelations(userId: String): List<ExpenseWithRelations>
    
    /**
     * Get expenses with their related category and payment method as Flow
     */
    @Transaction
    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND deletedAt IS NULL 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpensesWithRelationsFlow(userId: String): Flow<List<ExpenseWithRelations>>
    
    /**
     * Get expense with relations by ID
     */
    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :expenseId AND deletedAt IS NULL LIMIT 1")
    suspend fun getExpenseWithRelationsById(expenseId: String): ExpenseWithRelations?
}
