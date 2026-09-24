package com.example.smartbudget.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Expense table
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val categoryId: String,
    val paymentMethodId: String?,
    val amount: String, // Store as String to preserve decimal precision
    val description: String,
    val notes: String?,
    val date: String, // Store as String in YYYY-MM-DD format
    val receiptPhotoUrl: String?, // URL to receipt photo
    val syncStatus: String, // "synced", "pending", "conflict", "deleted"
    val syncVersion: Int,
    val localId: String?, // Client-generated UUID for pending sync
    val createdAt: Long, // Unix timestamp in milliseconds
    val updatedAt: Long,
    val deletedAt: Long? = null // Soft delete timestamp
)
