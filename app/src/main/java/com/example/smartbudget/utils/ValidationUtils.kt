package com.example.smartbudget.utils

import java.math.BigDecimal
import java.util.regex.Pattern

/**
 * Utility functions for input validation with user-friendly error messages
 */
object ValidationUtils {
    
    // Email validation pattern
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )
    
    // Password minimum requirements
    private const val PASSWORD_MIN_LENGTH = 8
    private const val NAME_MIN_LENGTH = 2
    private const val DESCRIPTION_MIN_LENGTH = 3
    
    /**
     * Validation result with specific error message
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
        
        val isValid: Boolean get() = this is Valid
        val errorMessage: String? get() = (this as? Invalid)?.message
    }
    
    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Email is required")
            !EMAIL_PATTERN.matcher(email).matches() -> 
                ValidationResult.Invalid("Please enter a valid email address")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate password
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Password is required")
            password.length < PASSWORD_MIN_LENGTH -> 
                ValidationResult.Invalid("Password must be at least $PASSWORD_MIN_LENGTH characters")
            !password.any { it.isDigit() } -> 
                ValidationResult.Invalid("Password must contain at least one number")
            !password.any { it.isUpperCase() } -> 
                ValidationResult.Invalid("Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } -> 
                ValidationResult.Invalid("Password must contain at least one lowercase letter")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate password confirmation
     */
    fun validatePasswordConfirmation(password: String, confirmation: String): ValidationResult {
        return when {
            confirmation.isBlank() -> ValidationResult.Invalid("Please confirm your password")
            password != confirmation -> ValidationResult.Invalid("Passwords do not match")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate name (user name, category name, etc.)
     */
    fun validateName(name: String, fieldName: String = "Name"): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("$fieldName is required")
            name.length < NAME_MIN_LENGTH -> 
                ValidationResult.Invalid("$fieldName must be at least $NAME_MIN_LENGTH characters")
            name.length > 50 -> 
                ValidationResult.Invalid("$fieldName must not exceed 50 characters")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> 
                ValidationResult.Invalid("$fieldName can only contain letters and spaces")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate full name (for user registration)
     */
    fun validateFullName(fullName: String): ValidationResult {
        return when {
            fullName.isBlank() -> ValidationResult.Invalid("Full name is required")
            fullName.length < NAME_MIN_LENGTH -> 
                ValidationResult.Invalid("Full name must be at least $NAME_MIN_LENGTH characters")
            fullName.length > 100 -> 
                ValidationResult.Invalid("Full name must not exceed 100 characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate currency code
     */
    fun validateCurrency(currency: String): ValidationResult {
        val validCurrencies = setOf("USD", "EUR", "GBP", "KES", "NGN", "ZAR", "JPY", "CNY", "INR")
        return when {
            currency.isBlank() -> ValidationResult.Invalid("Currency is required")
            !validCurrencies.contains(currency.uppercase()) -> 
                ValidationResult.Invalid("Please select a valid currency")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate description (expense description, income description, etc.)
     */
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Invalid("Description is required")
            description.length < DESCRIPTION_MIN_LENGTH -> 
                ValidationResult.Invalid("Description must be at least $DESCRIPTION_MIN_LENGTH characters")
            description.length > 200 -> 
                ValidationResult.Invalid("Description must not exceed 200 characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate amount (expense amount, income amount, etc.)
     */
    fun validateAmount(amount: String, fieldName: String = "Amount"): ValidationResult {
        return when {
            amount.isBlank() -> ValidationResult.Invalid("$fieldName is required")
            amount.toDoubleOrNull() == null -> 
                ValidationResult.Invalid("Please enter a valid $fieldName")
            amount.toDouble() <= 0 -> 
                ValidationResult.Invalid("$fieldName must be greater than 0")
            amount.toDouble() > 999999.99 -> 
                ValidationResult.Invalid("$fieldName is too large")
            else -> {
                // Validate decimal places (max 2)
                val parts = amount.split(".")
                if (parts.size > 1 && parts[1].length > 2) {
                    ValidationResult.Invalid("$fieldName can have at most 2 decimal places")
                } else {
                    ValidationResult.Valid
                }
            }
        }
    }
    
    /**
     * Validate currency amount and return BigDecimal if valid
     */
    fun parseCurrencyAmount(amount: String): Pair<BigDecimal?, ValidationResult> {
        val validation = validateAmount(amount)
        return if (validation.isValid) {
            BigDecimal(amount) to validation
        } else {
            null to validation
        }
    }
    
    /**
     * Validate notes (optional field)
     */
    fun validateNotes(notes: String): ValidationResult {
        return when {
            notes.length > 500 -> 
                ValidationResult.Invalid("Notes must not exceed 500 characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate category selection
     */
    fun validateCategorySelected(categoryId: String?): ValidationResult {
        return when {
            categoryId.isNullOrBlank() -> 
                ValidationResult.Invalid("Please select a category")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Format validation errors for display
     */
    fun formatValidationErrors(errors: List<String>): String {
        return when {
            errors.isEmpty() -> ""
            errors.size == 1 -> errors.first()
            else -> errors.joinToString("\n• ", prefix = "Please fix the following:\n• ")
        }
    }
    
    /**
     * Check if password is strong (optional stricter validation)
     */
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 12 &&
               password.any { it.isDigit() } &&
               password.any { it.isUpperCase() } &&
               password.any { it.isLowerCase() } &&
               password.any { !it.isLetterOrDigit() } // Special character
    }
    
    /**
     * Get password strength indicator
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 6 -> PasswordStrength.VERY_WEAK
            password.length < 8 -> PasswordStrength.WEAK
            isStrongPassword(password) -> PasswordStrength.STRONG
            password.length >= 8 && 
            password.any { it.isDigit() } && 
            password.any { it.isUpperCase() } -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    enum class PasswordStrength(val label: String, val color: Int) {
        VERY_WEAK("Very Weak", android.R.color.holo_red_dark),
        WEAK("Weak", android.R.color.holo_orange_dark),
        MEDIUM("Medium", android.R.color.holo_orange_light),
        STRONG("Strong", android.R.color.holo_green_dark)
    }
}
