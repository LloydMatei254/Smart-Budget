package com.example.smartbudget.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * Utility functions for date handling
 */
object DateUtils {
    
    private val apiDateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_API)
    private val displayDateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DISPLAY)
    private val displayDateShortFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DISPLAY_SHORT)
    
    /**
     * Format LocalDate to API format (YYYY-MM-DD)
     */
    fun formatToApi(date: LocalDate): String {
        return date.format(apiDateFormatter)
    }
    
    /**
     * Parse date from API format (YYYY-MM-DD)
     */
    fun parseFromApi(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, apiDateFormatter)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date format: $dateString", e)
        }
    }
    
    /**
     * Format LocalDate to display format (MMM dd, yyyy)
     */
    fun formatToDisplay(date: LocalDate): String {
        return date.format(displayDateFormatter)
    }
    
    /**
     * Format LocalDate to short display format (MMM dd)
     */
    fun formatToDisplayShort(date: LocalDate): String {
        return date.format(displayDateShortFormatter)
    }
    
    /**
     * Format Instant to display format
     */
    fun formatInstantToDisplay(instant: Instant): String {
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return formatToDisplay(localDate)
    }
    
    /**
     * Get relative date description (e.g., "Today", "Yesterday", "3 days ago")
     */
    fun getRelativeDateString(date: LocalDate): String {
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(date, today)
        
        return when {
            daysDiff == 0L -> "Today"
            daysDiff == 1L -> "Yesterday"
            daysDiff < 7 -> "$daysDiff days ago"
            daysDiff < 30 -> "${daysDiff / 7} weeks ago"
            daysDiff < 365 -> "${daysDiff / 30} months ago"
            else -> "${daysDiff / 365} years ago"
        }
    }
    
    /**
     * Format date as relative string (alias for getRelativeDateString)
     */
    fun formatRelativeDate(date: LocalDate): String {
        return getRelativeDateString(date)
    }
    
    /**
     * Get start of current month
     */
    fun getStartOfMonth(): LocalDate {
        return LocalDate.now().withDayOfMonth(1)
    }
    
    /**
     * Get end of current month
     */
    fun getEndOfMonth(): LocalDate {
        val now = LocalDate.now()
        return now.withDayOfMonth(now.lengthOfMonth())
    }
    
    /**
     * Get start of previous month
     */
    fun getStartOfPreviousMonth(): LocalDate {
        return LocalDate.now().minusMonths(1).withDayOfMonth(1)
    }
    
    /**
     * Get end of previous month
     */
    fun getEndOfPreviousMonth(): LocalDate {
        val previousMonth = LocalDate.now().minusMonths(1)
        return previousMonth.withDayOfMonth(previousMonth.lengthOfMonth())
    }
    
    /**
     * Get date N days ago
     */
    fun getDaysAgo(days: Long): LocalDate {
        return LocalDate.now().minusDays(days)
    }
    
    /**
     * Get date N months ago
     */
    fun getMonthsAgo(months: Long): LocalDate {
        return LocalDate.now().minusMonths(months)
    }
    
    /**
     * Check if date is today
     */
    fun isToday(date: LocalDate): Boolean {
        return date == LocalDate.now()
    }
    
    /**
     * Check if date is in current month
     */
    fun isCurrentMonth(date: LocalDate): Boolean {
        val now = LocalDate.now()
        return date.year == now.year && date.month == now.month
    }
    
    /**
     * Get month name from LocalDate
     */
    fun getMonthName(date: LocalDate): String {
        return date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Convert Instant to LocalDate
     */
    fun instantToLocalDate(instant: Instant): LocalDate {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate()
    }
    
    /**
     * Convert LocalDate to Instant (at start of day)
     */
    fun localDateToInstant(date: LocalDate): Instant {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant()
    }
}
