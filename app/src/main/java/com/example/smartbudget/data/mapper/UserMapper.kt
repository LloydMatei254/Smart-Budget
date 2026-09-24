package com.example.smartbudget.data.mapper

import com.example.smartbudget.data.local.entities.UserEntity
import com.example.smartbudget.data.remote.dto.response.UserDto
import com.example.smartbudget.domain.model.Currency
import com.example.smartbudget.domain.model.User
import java.time.Instant

/**
 * Mapper for converting between User representations
 */
object UserMapper {
    
    /**
     * Convert UserDto (API response) to User (domain model)
     */
    fun UserDto.toDomain(): User {
        return User(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            currency = Currency.fromCode(this.currency),
            createdAt = Instant.parse(this.createdAt),
            updatedAt = Instant.parse(this.updatedAt)
        )
    }
    
    /**
     * Convert UserEntity (Room) to User (domain model)
     */
    fun UserEntity.toDomain(): User {
        return User(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            currency = Currency.fromCode(this.currency),
            createdAt = Instant.ofEpochMilli(this.createdAt),
            updatedAt = Instant.ofEpochMilli(this.updatedAt)
        )
    }
    
    /**
     * Convert User (domain model) to UserEntity (Room)
     */
    fun User.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            currency = this.currency.code,
            createdAt = this.createdAt.toEpochMilli(),
            updatedAt = this.updatedAt.toEpochMilli()
        )
    }
    
    /**
     * Convert UserDto to UserEntity
     */
    fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            fullName = this.fullName,
            email = this.email,
            currency = this.currency,
            createdAt = Instant.parse(this.createdAt).toEpochMilli(),
            updatedAt = Instant.parse(this.updatedAt).toEpochMilli()
        )
    }
}
