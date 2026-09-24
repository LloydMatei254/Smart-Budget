package com.example.smartbudget.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Domain model representing an expense transaction.
 */
data class Expense(
    val id: String,
    val userId: String,
    val categoryId: String,
    val paymentMethodId: String?,
    val amount: BigDecimal,
    val description: String,
    val notes: String?,
    val date: LocalDate,
    val receiptPhotoUrl: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncVersion: Int = 1,
    val localId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * Check if this expense is pending synchronization
     */
    fun isPendingSync(): Boolean = syncStatus == SyncStatus.PENDING

    /**
     * Check if this expense has a conflict
     */
    fun hasConflict(): Boolean = syncStatus == SyncStatus.CONFLICT

    /**
     * Create a copy with updated sync status
     */
    fun withSyncStatus(status: SyncStatus): Expense {
        return copy(syncStatus = status, updatedAt = Instant.now())
    }

    /**
     * Create a copy with incremented version
     */
    fun incrementVersion(): Expense {
        return copy(syncVersion = syncVersion + 1, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            userId: String,
            categoryId: String,
            paymentMethodId: String?,
            amount: BigDecimal,
            description: String,
            notes: String? = null,
            date: LocalDate = LocalDate.now(),
            receiptPhotoUrl: String? = null,
            syncStatus: SyncStatus = SyncStatus.PENDING
        ): Expense {
            val now = Instant.now()
            val localId = UUID.randomUUID().toString()
            return Expense(
                id = "", // Will be assigned by server
                userId = userId,
                categoryId = categoryId,
                paymentMethodId = paymentMethodId,
                amount = amount,
                description = description,
                notes = notes,
                date = date,
                receiptPhotoUrl = receiptPhotoUrl,
                syncStatus = syncStatus,
                syncVersion = 1,
                localId = localId,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Expense with related data for display purposes
 */
data class ExpenseWithDetails(
    val expense: Expense,
    val category: Category,
    val paymentMethod: PaymentMethod?
) {
    val id: String get() = expense.id
    val amount: BigDecimal get() = expense.amount
    val description: String get() = expense.description
    val date: LocalDate get() = expense.date
    val categoryName: String get() = category.name
    val categoryColor: String get() = category.color
    val categoryIcon: String get() = category.icon
    val paymentMethodName: String? get() = paymentMethod?.name
}
