package com.example.smartbudget.domain.repository

import com.example.smartbudget.domain.model.CategorySpending
import com.example.smartbudget.domain.model.FinancialSummary
import com.example.smartbudget.domain.model.MonthlyTrend
import java.time.LocalDate

/**
 * Repository interface for Report operations
 */
interface ReportRepository {
    
    /**
     * Get financial summary for date range
     */
    suspend fun getFinancialSummary(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<FinancialSummary>
    
    /**
     * Get spending by category
     */
    suspend fun getSpendingByCategory(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<CategorySpending>>
    
    /**
     * Get monthly trends
     */
    suspend fun getMonthlyTrends(months: Int = 6): Result<List<MonthlyTrend>>
    
    /**
     * Get income vs expenses comparison
     */
    suspend fun getIncomeVsExpenses(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: String = "month"
    ): Result<Map<String, Any>>
}
