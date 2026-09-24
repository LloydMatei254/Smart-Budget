package com.example.smartbudget.utils

import com.example.smartbudget.domain.model.Currency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility functions for currency formatting
 */
object CurrencyUtils {
    
    /**
     * Format amount with currency symbol
     */
    fun formatAmount(amount: BigDecimal, currency: Currency = Currency.USD): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formattedAmount = formatter.format(amount)
        return "${currency.symbol}$formattedAmount"
    }
    
    /**
     * Format amount with currency code
     */
    fun formatAmountWithCode(amount: BigDecimal, currency: Currency): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formattedAmount = formatter.format(amount)
        return "$formattedAmount ${currency.code}"
    }
    
    /**
     * Format amount without currency symbol (for input fields)
     */
    fun formatAmountPlain(amount: BigDecimal): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }
    
    /**
     * Parse amount string to BigDecimal
     */
    fun parseAmount(amountString: String): BigDecimal? {
        return try {
            // Remove commas and whitespace
            val cleaned = amountString.replace(",", "").replace(" ", "")
            BigDecimal(cleaned)
        } catch (e: NumberFormatException) {
            null
        }
    }
    
    /**
     * Format amount for expense (with minus sign and red color indicator)
     */
    fun formatExpenseAmount(amount: BigDecimal, currency: Currency): String {
        return "-${formatAmount(amount, currency)}"
    }
    
    /**
     * Format amount for income (with plus sign and green color indicator)
     */
    fun formatIncomeAmount(amount: BigDecimal, currency: Currency): String {
        return "+${formatAmount(amount, currency)}"
    }
    
    /**
     * Format large amounts with K, M, B suffixes
     */
    fun formatCompactAmount(amount: BigDecimal, currency: Currency): String {
        val absAmount = amount.abs()
        
        return when {
            absAmount >= BigDecimal("1000000000") -> {
                val billions = absAmount.divide(BigDecimal("1000000000"), 1, BigDecimal.ROUND_HALF_UP)
                "${currency.symbol}${billions}B"
            }
            absAmount >= BigDecimal("1000000") -> {
                val millions = absAmount.divide(BigDecimal("1000000"), 1, BigDecimal.ROUND_HALF_UP)
                "${currency.symbol}${millions}M"
            }
            absAmount >= BigDecimal("1000") -> {
                val thousands = absAmount.divide(BigDecimal("1000"), 1, BigDecimal.ROUND_HALF_UP)
                "${currency.symbol}${thousands}K"
            }
            else -> formatAmount(amount, currency)
        }
    }
    
    /**
     * Format percentage
     */
    fun formatPercentage(percentage: Float): String {
        return if (percentage >= 0) {
            "+${String.format("%.1f", percentage)}%"
        } else {
            "${String.format("%.1f", percentage)}%"
        }
    }
    
    /**
     * Format percentage without sign
     */
    fun formatPercentagePlain(percentage: Float): String {
        return "${String.format("%.1f", percentage)}%"
    }
    
    /**
     * Check if amount is negative
     */
    fun isNegative(amount: BigDecimal): Boolean {
        return amount < BigDecimal.ZERO
    }
    
    /**
     * Check if amount is positive
     */
    fun isPositive(amount: BigDecimal): Boolean {
        return amount > BigDecimal.ZERO
    }
    
    /**
     * Check if amount is zero
     */
    fun isZero(amount: BigDecimal): Boolean {
        return amount.compareTo(BigDecimal.ZERO) == 0
    }
    
    /**
     * Calculate percentage of total
     */
    fun calculatePercentage(amount: BigDecimal, total: BigDecimal): Float {
        if (total == BigDecimal.ZERO) return 0f
        return (amount.divide(total, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal("100")).toFloat()
    }
    
    /**
     * Round amount to 2 decimal places
     */
    fun roundAmount(amount: BigDecimal): BigDecimal {
        return amount.setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}
