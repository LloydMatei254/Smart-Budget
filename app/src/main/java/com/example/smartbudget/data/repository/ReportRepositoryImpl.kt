package com.example.smartbudget.data.repository

import com.example.smartbudget.data.remote.api.ReportsApi
import com.example.smartbudget.data.remote.dto.response.CategorySpendingDto
import com.example.smartbudget.data.remote.dto.response.FinancialSummaryDto
import com.example.smartbudget.data.remote.dto.response.MonthlyTrendDto
import com.example.smartbudget.di.IoDispatcher
import com.example.smartbudget.domain.model.CategorySpending
import com.example.smartbudget.domain.model.FinancialSummary
import com.example.smartbudget.domain.model.MonthlyTrend
import com.example.smartbudget.domain.repository.ReportRepository
import com.example.smartbudget.utils.NetworkUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportsApi: ReportsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReportRepository {
    
    override suspend fun getFinancialSummary(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<FinancialSummary> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                reportsApi.getFinancialSummary(
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )
            }
            
            result.map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get financial summary")
            Result.failure(e)
        }
    }
    
    override suspend fun getSpendingByCategory(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<CategorySpending>> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                reportsApi.getSpendingByCategory(
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )
            }
            
            result.map { dtos -> dtos.map { it.toDomain() } }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get spending by category")
            Result.failure(e)
        }
    }
    
    override suspend fun getMonthlyTrends(months: Int): Result<List<MonthlyTrend>> =
        withContext(ioDispatcher) {
            try {
                if (!NetworkUtils.isNetworkAvailable()) {
                    return@withContext Result.failure(Exception("No network connection"))
                }
                
                val result = NetworkUtils.safeApiCall {
                    reportsApi.getMonthlyTrends(months = months)
                }
                
                result.map { dtos -> dtos.map { it.toDomain() } }
            } catch (e: Exception) {
                Timber.e(e, "Failed to get monthly trends")
                Result.failure(e)
            }
        }
    
    override suspend fun getIncomeVsExpenses(
        startDate: LocalDate,
        endDate: LocalDate,
        groupBy: String
    ): Result<Map<String, Any>> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                reportsApi.getIncomeVsExpenses(
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                    groupBy = groupBy
                )
            }
            
            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to get income vs expenses")
            Result.failure(e)
        }
    }
    
    // Extension functions to convert DTOs to domain models
    private fun FinancialSummaryDto.toDomain(): FinancialSummary {
        // Note: Currency is not in the DTO, would need to be fetched from user preferences
        // For now, defaulting to USD
        val dateRange = if (this.period != null) {
            com.example.smartbudget.domain.model.DateRange(
                startDate = LocalDate.parse(this.period.startDate),
                endDate = LocalDate.parse(this.period.endDate)
            )
        } else {
            // Fallback to current month if period is not provided
            val now = LocalDate.now()
            com.example.smartbudget.domain.model.DateRange(
                startDate = now.withDayOfMonth(1),
                endDate = now
            )
        }
        
        return FinancialSummary(
            totalIncome = BigDecimal(this.totalIncome),
            totalExpenses = BigDecimal(this.totalExpenses),
            balance = BigDecimal(this.balance),
            incomeCount = this.incomeCount,
            expenseCount = this.expenseCount,
            currency = com.example.smartbudget.domain.model.Currency.USD, // TODO: Get from user preferences
            period = dateRange
        )
    }
    
    private fun CategorySpendingDto.toDomain(): CategorySpending {
        // Need to create a minimal Category object for CategorySpending
        val category = com.example.smartbudget.domain.model.Category(
            id = this.categoryId,
            userId = "", // Not provided in DTO
            name = this.categoryName,
            color = this.categoryColor,
            icon = this.categoryIcon,
            isDefault = false,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        
        return CategorySpending(
            category = category,
            totalAmount = BigDecimal(this.totalAmount),
            transactionCount = this.transactionCount,
            percentage = this.percentage
        )
    }
    
    private fun MonthlyTrendDto.toDomain(): MonthlyTrend {
        return MonthlyTrend(
            month = LocalDate.parse(this.month),
            totalIncome = BigDecimal(this.totalIncome),
            totalExpenses = BigDecimal(this.totalExpenses),
            netBalance = BigDecimal(this.netBalance)
        )
    }
}
