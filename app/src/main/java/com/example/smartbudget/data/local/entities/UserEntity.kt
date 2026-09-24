package com.example.smartbudget.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for User table
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val email: String,
    val currency: String,
    val createdAt: Long, // Unix timestamp in milliseconds
    val updatedAt: Long
)
