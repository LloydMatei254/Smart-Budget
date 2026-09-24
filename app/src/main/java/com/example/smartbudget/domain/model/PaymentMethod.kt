package com.example.smartbudget.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a payment method.
 */
data class PaymentMethod(
    val id: String,
    val userId: String,
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            userId: String,
            name: String,
            isDefault: Boolean = false
        ): PaymentMethod {
            val now = Instant.now()
            return PaymentMethod(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                isDefault = isDefault,
                createdAt = now,
                updatedAt = now
            )
        }

        /**
         * Default payment methods for new users
         */
        fun getDefaultPaymentMethods(userId: String): List<PaymentMethod> {
            val now = Instant.now()
            return listOf(
                PaymentMethod(UUID.randomUUID().toString(), userId, "Cash", true, now, now),
                PaymentMethod(UUID.randomUUID().toString(), userId, "Card", false, now, now),
                PaymentMethod(UUID.randomUUID().toString(), userId, "Bank Transfer", false, now, now),
                PaymentMethod(UUID.randomUUID().toString(), userId, "Mobile Money", false, now, now),
                PaymentMethod(UUID.randomUUID().toString(), userId, "Other", false, now, now)
            )
        }
    }
}
