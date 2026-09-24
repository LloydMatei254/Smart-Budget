package com.example.smartbudget.domain.usecase.auth

import com.example.smartbudget.domain.model.User
import com.example.smartbudget.domain.repository.AuthRepository
import com.example.smartbudget.utils.ValidationUtils
import javax.inject.Inject

/**
 * Use case for user login
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Execute login
     */
    suspend operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Result<User> {
        // Validate email
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            return Result.failure(
                ValidationException(mapOf("email" to emailValidation.errorMessage!!))
            )
        }
        
        // Validate password
        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.isValid) {
            return Result.failure(
                ValidationException(mapOf("password" to passwordValidation.errorMessage!!))
            )
        }
        
        // Perform login
        return authRepository.login(
            email = email.trim(),
            password = password,
            rememberMe = rememberMe
        )
    }
}

/**
 * Exception thrown when validation fails
 */
class ValidationException(
    val errors: Map<String, String>
) : Exception("Validation failed: ${errors.values.joinToString()}")
