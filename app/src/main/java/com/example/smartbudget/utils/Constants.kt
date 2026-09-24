package com.example.smartbudget.utils

/**
 * Application-wide constants
 */
object Constants {
    
    // Date formats
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val DATE_FORMAT_DISPLAY_SHORT = "MMM dd"
    const val TIMESTAMP_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    // Token expiry buffer (refresh token 5 minutes before expiry)
    const val TOKEN_EXPIRY_BUFFER_MS = 5 * 60 * 1000L
    
    // Sync intervals
    const val SYNC_INTERVAL_MINUTES = 30L
    const val SYNC_FLEX_INTERVAL_MINUTES = 15L
    
    // Amount constraints
    const val MIN_AMOUNT = 0.01
    const val MAX_AMOUNT = 9999999999999.99
    const val AMOUNT_DECIMAL_PLACES = 2
    
    // Text length constraints
    const val MAX_DESCRIPTION_LENGTH = 255
    const val MAX_NOTES_LENGTH = 1000
    const val MAX_CATEGORY_NAME_LENGTH = 100
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 128
    
    // Default currency
    const val DEFAULT_CURRENCY = "USD"
    
    // Shared preferences keys (deprecated, use PreferencesManager instead)
    @Deprecated("Use PreferencesManager")
    const val PREF_USER_ID = "user_id"
    
    // Intent extras
    const val EXTRA_EXPENSE_ID = "expense_id"
    const val EXTRA_INCOME_ID = "income_id"
    const val EXTRA_CATEGORY_ID = "category_id"
    const val EXTRA_TRANSACTION_TYPE = "transaction_type"
    
    // Transaction types
    const val TRANSACTION_TYPE_EXPENSE = "expense"
    const val TRANSACTION_TYPE_INCOME = "income"
    
    // Navigation arguments
    const val ARG_EXPENSE_ID = "expenseId"
    const val ARG_INCOME_ID = "incomeId"
    const val ARG_CATEGORY_ID = "categoryId"
    
    // Database
    const val DATABASE_NAME = "smart_budget_database"
    const val DATABASE_VERSION = 1
    
    // WorkManager tags
    const val WORK_TAG_SYNC = "sync_work"
    const val WORK_NAME_PERIODIC_SYNC = "periodic_sync"
    const val WORK_NAME_ONE_TIME_SYNC = "one_time_sync"
    
    // Notification channels
    const val NOTIFICATION_CHANNEL_SYNC = "sync_notifications"
    const val NOTIFICATION_CHANNEL_GENERAL = "general_notifications"
    
    // Animation durations
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    
    // Chart colors (for spending by category)
    val CHART_COLORS = listOf(
        "#FF6B6B", // Red
        "#4ECDC4", // Turquoise
        "#45B7D1", // Blue
        "#FFA07A", // Light Salmon
        "#98D8C8", // Mint
        "#F7DC6F", // Yellow
        "#E74C3C", // Dark Red
        "#3498DB", // Blue
        "#9B59B6", // Purple
        "#FF69B4", // Pink
        "#34495E", // Dark Gray
        "#27AE60", // Green
        "#E67E22", // Orange
        "#95A5A6"  // Gray
    )
    
    // Material icons for categories (icon names)
    object CategoryIcons {
        const val RESTAURANT = "restaurant"
        const val DIRECTIONS_CAR = "directions_car"
        const val HOME = "home"
        const val BOLT = "bolt"
        const val SHOPPING_BAG = "shopping_bag"
        const val MOVIE = "movie"
        const val LOCAL_HOSPITAL = "local_hospital"
        const val SCHOOL = "school"
        const val FLIGHT = "flight"
        const val SPA = "spa"
        const val SECURITY = "security"
        const val SAVINGS = "savings"
        const val CARD_GIFTCARD = "card_giftcard"
        const val CATEGORY = "category"
        const val MORE_HORIZ = "more_horiz"
    }
}
