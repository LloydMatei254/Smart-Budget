package com.example.smartbudget.domain.usecase.auth

import com.example.smartbudget.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user logout
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Execute logout
     */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
