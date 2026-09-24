package com.example.smartbudget.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing an expense category.
 */
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val color: String, // Hex color code (e.g., "#FF6B6B")
    val icon: String, // Material icon name
    val isDefault: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            userId: String,
            name: String,
            color: String,
            icon: String,
            isDefault: Boolean = false
        ): Category {
            val now = Instant.now()
            return Category(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                color = color,
                icon = icon,
                isDefault = isDefault,
                createdAt = now,
                updatedAt = now
            )
        }

        /**
         * Default categories with their icons and colors
         */
        fun getDefaultCategories(userId: String): List<Category> {
            val now = Instant.now()
            return listOf(
                Category(UUID.randomUUID().toString(), userId, "Food & Dining", "#FF6B6B", "restaurant", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Transport", "#4ECDC4", "directions_car", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Housing", "#45B7D1", "home", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Utilities", "#FFA07A", "bolt", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Shopping", "#98D8C8", "shopping_bag", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Entertainment", "#F7DC6F", "movie", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Health", "#E74C3C", "local_hospital", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Education", "#3498DB", "school", true, now, now),
                Category(UUID.randomUUID().toString(), userId, "Other", "#95A5A6", "more_horiz", true, now, now)
            )
        }
    }
}
