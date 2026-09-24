package com.example.smartbudget.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a user in the Smart Budget application.
 * This is the business logic representation, independent of data sources.
 */
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val currency: Currency,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            fullName: String,
            email: String,
            currency: Currency = Currency.USD
        ): User {
            val now = Instant.now()
            return User(
                id = UUID.randomUUID().toString(),
                fullName = fullName,
                email = email,
                currency = currency,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
