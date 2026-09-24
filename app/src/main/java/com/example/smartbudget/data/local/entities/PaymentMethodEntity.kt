package com.example.smartbudget.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Payment Method table
 */
@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val isDefault: Boolean,
    val createdAt: Long, // Unix timestamp in milliseconds
    val updatedAt: Long
)
