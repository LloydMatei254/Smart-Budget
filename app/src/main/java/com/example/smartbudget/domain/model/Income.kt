package com.example.smartbudget.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Domain model representing an income transaction.
 */
data class Income(
    val id: String,
    val userId: String,
    val amount: BigDecimal,
    val source: IncomeSource,
    val description: String,
    val notes: String?,
    val date: LocalDate,
    val isRecurring: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val syncVersion: Int = 1,
    val localId: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * Check if this income is pending synchronization
     */
    fun isPendingSync(): Boolean = syncStatus == SyncStatus.PENDING

    /**
     * Check if this income has a conflict
     */
    fun hasConflict(): Boolean = syncStatus == SyncStatus.CONFLICT

    /**
     * Create a copy with updated sync status
     */
    fun withSyncStatus(status: SyncStatus): Income {
        return copy(syncStatus = status, updatedAt = Instant.now())
    }

    /**
     * Create a copy with incremented version
     */
    fun incrementVersion(): Income {
        return copy(syncVersion = syncVersion + 1, updatedAt = Instant.now())
    }

    companion object {
        fun create(
            userId: String,
            amount: BigDecimal,
            source: IncomeSource,
            description: String,
            notes: String? = null,
            date: LocalDate = LocalDate.now(),
            isRecurring: Boolean = false,
            syncStatus: SyncStatus = SyncStatus.PENDING
        ): Income {
            val now = Instant.now()
            val localId = UUID.randomUUID().toString()
            return Income(
                id = "", // Will be assigned by server
                userId = userId,
                amount = amount,
                source = source,
                description = description,
                notes = notes,
                date = date,
                isRecurring = isRecurring,
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
 * Income source types
 */
enum class IncomeSource(val displayName: String) {
    SALARY("Salary"),
    FREELANCE("Freelance"),
    BUSINESS("Business"),
    INVESTMENT("Investment"),
    RENTAL("Rental Income"),
    GIFT("Gift"),
    REFUND("Refund"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): IncomeSource {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }

        fun getAll(): List<IncomeSource> = values().toList()
    }

    override fun toString(): String = displayName
}
