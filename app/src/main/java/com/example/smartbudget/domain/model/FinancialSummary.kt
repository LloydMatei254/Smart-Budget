package com.example.smartbudget.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Financial summary for the dashboard
 */
data class FinancialSummary(
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val balance: BigDecimal,
    val incomeCount: Int,
    val expenseCount: Int,
    val currency: Currency,
    val period: DateRange?
) {
    /**
     * Calculate percentage change in income
     */
    fun incomeChangePercentage(previousIncome: BigDecimal): Double {
        if (previousIncome == BigDecimal.ZERO) return 0.0
        return ((totalIncome - previousIncome) / previousIncome * BigDecimal(100)).toDouble()
    }

    /**
     * Calculate percentage change in expenses
     */
    fun expenseChangePercentage(previousExpenses: BigDecimal): Double {
        if (previousExpenses == BigDecimal.ZERO) return 0.0
        return ((totalExpenses - previousExpenses) / previousExpenses * BigDecimal(100)).toDouble()
    }

    companion object {
        fun empty(currency: Currency = Currency.USD): FinancialSummary {
            return FinancialSummary(
                totalIncome = BigDecimal.ZERO,
                totalExpenses = BigDecimal.ZERO,
                balance = BigDecimal.ZERO,
                incomeCount = 0,
                expenseCount = 0,
                currency = currency,
                period = null
            )
        }
    }
}

/**
 * Date range for financial summaries and reports
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        fun thisMonth(): DateRange {
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1)
            val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
            return DateRange(startOfMonth, endOfMonth)
        }

        fun lastMonth(): DateRange {
            val now = LocalDate.now()
            val lastMonth = now.minusMonths(1)
            val startOfMonth = lastMonth.withDayOfMonth(1)
            val endOfMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
            return DateRange(startOfMonth, endOfMonth)
        }

        fun last30Days(): DateRange {
            val now = LocalDate.now()
            return DateRange(now.minusDays(30), now)
        }

        fun thisYear(): DateRange {
            val now = LocalDate.now()
            val startOfYear = now.withDayOfYear(1)
            return DateRange(startOfYear, now)
        }

        fun custom(startDate: LocalDate, endDate: LocalDate): DateRange {
            return DateRange(startDate, endDate)
        }
    }
}

/**
 * Spending by category for pie chart
 */
data class CategorySpending(
    val category: Category,
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val percentage: Float
)

/**
 * Monthly trend data for line/bar chart
 */
data class MonthlyTrend(
    val month: LocalDate, // First day of the month
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netBalance: BigDecimal
) {
    val monthName: String
        get() = month.month.name.lowercase().replaceFirstChar { it.uppercase() }
}

/**
 * Report period options
 */
enum class ReportPeriod(val displayName: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom Range");

    companion object {
        fun getAll(): List<ReportPeriod> = values().toList()
    }

    override fun toString(): String = displayName
}
