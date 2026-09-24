package com.example.smartbudget.data.mapper

import com.example.smartbudget.data.local.entities.PaymentMethodEntity
import com.example.smartbudget.data.remote.dto.response.PaymentMethodDto
import com.example.smartbudget.domain.model.PaymentMethod
import java.time.Instant

/**
 * Mapper for converting between PaymentMethod representations
 */
object PaymentMethodMapper {
    
    /**
     * Convert PaymentMethodDto (API response) to PaymentMethod (domain model)
     */
    fun PaymentMethodDto.toDomain(): PaymentMethod {
        return PaymentMethod(
            id = this.id,
            userId = "", // userId not included in API response
            name = this.name,
            isDefault = this.isDefault,
            createdAt = Instant.parse(this.createdAt),
            updatedAt = Instant.parse(this.updatedAt)
        )
    }
    
    /**
     * Convert PaymentMethodEntity (Room) to PaymentMethod (domain model)
     */
    fun PaymentMethodEntity.toDomain(): PaymentMethod {
        return PaymentMethod(
            id = this.id,
            userId = this.userId,
            name = this.name,
            isDefault = this.isDefault,
            createdAt = Instant.ofEpochMilli(this.createdAt),
            updatedAt = Instant.ofEpochMilli(this.updatedAt)
        )
    }
    
    /**
     * Convert PaymentMethod (domain model) to PaymentMethodEntity (Room)
     */
    fun PaymentMethod.toEntity(): PaymentMethodEntity {
        return PaymentMethodEntity(
            id = this.id,
            userId = this.userId,
            name = this.name,
            isDefault = this.isDefault,
            createdAt = this.createdAt.toEpochMilli(),
            updatedAt = this.updatedAt.toEpochMilli()
        )
    }
    
    /**
     * Convert PaymentMethodDto to PaymentMethodEntity
     */
    fun PaymentMethodDto.toEntity(userId: String): PaymentMethodEntity {
        return PaymentMethodEntity(
            id = this.id,
            userId = userId,
            name = this.name,
            isDefault = this.isDefault,
            createdAt = Instant.parse(this.createdAt).toEpochMilli(),
            updatedAt = Instant.parse(this.updatedAt).toEpochMilli()
        )
    }
}
