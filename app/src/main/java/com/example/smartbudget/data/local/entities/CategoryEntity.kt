package com.example.smartbudget.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Category table
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val localId: String, // Local-only ID before sync
    val userId: String,
    val name: String,
    val color: String, // Hex color code (e.g., "#FF6B6B")
    val icon: String, // Material icon name
    val isDefault: Boolean,
    val createdAt: Long, // Unix timestamp in milliseconds
    val updatedAt: Long,
    val deletedAt: Long? = null, // Soft delete timestamp
    val syncStatus: String = "SYNCED" // SYNCED, PENDING, FAILED
)
