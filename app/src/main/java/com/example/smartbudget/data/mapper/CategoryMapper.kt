package com.example.smartbudget.data.mapper

import com.example.smartbudget.data.local.entities.CategoryEntity
import com.example.smartbudget.data.remote.dto.response.CategoryDto
import com.example.smartbudget.domain.model.Category
import java.time.Instant

/**
 * Mapper for converting between Category representations
 */
object CategoryMapper {
    
    /**
     * Convert CategoryDto (API response) to Category (domain model)
     */
    fun CategoryDto.toDomain(): Category {
        return Category(
            id = this.id,
            userId = "", // userId not included in API response
            name = this.name,
            color = this.color,
            icon = this.icon,
            isDefault = this.isDefault,
            createdAt = Instant.parse(this.createdAt),
            updatedAt = Instant.parse(this.updatedAt)
        )
    }
    
    /**
     * Convert CategoryEntity (Room) to Category (domain model)
     */
    fun CategoryEntity.toDomain(): Category {
        return Category(
            id = this.id,
            userId = this.userId,
            name = this.name,
            color = this.color,
            icon = this.icon,
            isDefault = this.isDefault,
            createdAt = Instant.ofEpochMilli(this.createdAt),
            updatedAt = Instant.ofEpochMilli(this.updatedAt)
        )
    }
    
    /**
     * Convert Category (domain model) to CategoryEntity (Room)
     */
    fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = this.id,
            localId = this.id, // Use same ID as localId for synced items
            userId = this.userId,
            name = this.name,
            color = this.color,
            icon = this.icon,
            isDefault = this.isDefault,
            createdAt = this.createdAt.toEpochMilli(),
            updatedAt = this.updatedAt.toEpochMilli(),
            deletedAt = null,
            syncStatus = "SYNCED"
        )
    }
    
    /**
     * Convert CategoryDto to CategoryEntity
     */
    fun CategoryDto.toEntity(userId: String): CategoryEntity {
        return CategoryEntity(
            id = this.id,
            localId = this.id, // Use same ID as localId for synced items
            userId = userId,
            name = this.name,
            color = this.color,
            icon = this.icon,
            isDefault = this.isDefault,
            createdAt = Instant.parse(this.createdAt).toEpochMilli(),
            updatedAt = Instant.parse(this.updatedAt).toEpochMilli(),
            deletedAt = null,
            syncStatus = "SYNCED"
        )
    }
}
