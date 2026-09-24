package com.example.smartbudget.domain.usecase.auth

import com.example.smartbudget.domain.model.User
import com.example.smartbudget.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for getting current authenticated user
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Execute get current user
     */
    suspend operator fun invoke(): Result<User?> {
        return authRepository.getCurrentUser()
    }
}
