package com.example.smartbudget.domain.repository

import com.example.smartbudget.domain.model.Expense
import com.example.smartbudget.domain.model.ExpenseWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Expense operations
 */
interface ExpenseRepository {
    
    /**
     * Get all expenses as Flow
     */
    fun getExpensesFlow(): Flow<List<Expense>>
    
    /**
     * Get expenses with details as Flow
     */
    fun getExpensesWithDetailsFlow(): Flow<List<ExpenseWithDetails>>
    
    /**
     * Get expenses for date range
     */
    suspend fun getExpensesForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Expense>
    
    /**
     * Get expenses for date range as Flow
     */
    fun getExpensesForDateRangeFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Expense>>
    
    /**
     * Get expense by ID
     */
    suspend fun getExpenseById(expenseId: String): Expense?
    
    /**
     * Get expense with details by ID
     */
    suspend fun getExpenseWithDetailsById(expenseId: String): ExpenseWithDetails?
    
    /**
     * Get expenses by category
     */
    suspend fun getExpensesByCategory(categoryId: String): List<Expense>
    
    /**
     * Create new expense (offline-first)
     */
    suspend fun createExpense(expense: Expense): Result<Expense>
    
    /**
     * Update expense (offline-first)
     */
    suspend fun updateExpense(expense: Expense): Result<Expense>
    
    /**
     * Delete expense (offline-first soft delete)
     */
    suspend fun deleteExpense(expenseId: String): Result<Unit>
    
    /**
     * Sync expenses with server
     */
    suspend fun syncExpenses(): Result<Unit>
    
    /**
     * Force refresh from server
     */
    suspend fun refreshExpenses(): Result<Unit>
}
