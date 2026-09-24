package com.example.smartbudget.domain.usecase.auth

import com.example.smartbudget.domain.model.User
import com.example.smartbudget.domain.repository.AuthRepository
import com.example.smartbudget.utils.ValidationUtils
import javax.inject.Inject

/**
 * Use case for user registration
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Execute registration
     */
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        currency: String
    ): Result<User> {
        val errors = mutableMapOf<String, String>()
        
        // Validate full name
        val nameValidation = ValidationUtils.validateFullName(fullName)
        if (!nameValidation.isValid) {
            errors["fullName"] = nameValidation.errorMessage!!
        }
        
        // Validate email
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            errors["email"] = emailValidation.errorMessage!!
        }
        
        // Validate password
        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.isValid) {
            errors["password"] = passwordValidation.errorMessage!!
        }
        
        // Validate password confirmation
        if (password != confirmPassword) {
            errors["confirmPassword"] = "Passwords do not match"
        }
        
        // Validate currency
        val currencyValidation = ValidationUtils.validateCurrency(currency)
        if (!currencyValidation.isValid) {
            errors["currency"] = currencyValidation.errorMessage!!
        }
        
        // Return validation errors if any
        if (errors.isNotEmpty()) {
            return Result.failure(ValidationException(errors))
        }
        
        // Perform registration
        return authRepository.register(
            fullName = fullName.trim(),
            email = email.trim(),
            password = password,
            currency = currency
        )
    }
}
