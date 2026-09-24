package com.example.smartbudget.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Income table
 */
@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: String, // Store as String to preserve decimal precision
    val source: String, // Income source type
    val description: String,
    val notes: String?,
    val date: String, // Store as String in YYYY-MM-DD format
    val isRecurring: Boolean, // Whether this is recurring income
    val syncStatus: String, // "synced", "pending", "conflict", "deleted"
    val syncVersion: Int,
    val localId: String?, // Client-generated UUID for pending sync
    val createdAt: Long, // Unix timestamp in milliseconds
    val updatedAt: Long,
    val deletedAt: Long? = null // Soft delete timestamp
)
