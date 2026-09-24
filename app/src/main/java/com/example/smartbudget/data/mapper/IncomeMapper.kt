package com.example.smartbudget.data.mapper

import com.example.smartbudget.data.local.entities.IncomeEntity
import com.example.smartbudget.data.remote.dto.response.IncomeDto
import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.domain.model.IncomeSource
import com.example.smartbudget.domain.model.SyncStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Mapper for converting between Income representations
 */
object IncomeMapper {
    
    /**
     * Convert IncomeDto (API response) to Income (domain model)
     */
    fun IncomeDto.toDomain(): Income {
        return Income(
            id = this.id,
            userId = "", // userId not included in API response
            amount = BigDecimal(this.amount),
            source = IncomeSource.fromString(this.source),
            description = this.description,
            notes = this.notes,
            date = LocalDate.parse(this.date),
            isRecurring = false, // isRecurring not in DTO yet
            syncStatus = SyncStatus.fromString(this.syncStatus),
            syncVersion = this.syncVersion,
            localId = null,
            createdAt = Instant.parse(this.createdAt),
            updatedAt = Instant.parse(this.updatedAt)
        )
    }
    
    /**
     * Convert IncomeEntity (Room) to Income (domain model)
     */
    fun IncomeEntity.toDomain(): Income {
        return Income(
            id = this.id,
            userId = this.userId,
            amount = BigDecimal(this.amount),
            source = IncomeSource.fromString(this.source),
            description = this.description,
            notes = this.notes,
            date = LocalDate.parse(this.date),
            isRecurring = this.isRecurring,
            syncStatus = SyncStatus.fromString(this.syncStatus),
            syncVersion = this.syncVersion,
            localId = this.localId,
            createdAt = Instant.ofEpochMilli(this.createdAt),
            updatedAt = Instant.ofEpochMilli(this.updatedAt)
        )
    }
    
    /**
     * Convert Income (domain model) to IncomeEntity (Room)
     */
    fun Income.toEntity(): IncomeEntity {
        return IncomeEntity(
            id = this.id,
            userId = this.userId,
            amount = this.amount.toPlainString(),
            source = this.source.name,
            description = this.description,
            notes = this.notes,
            date = this.date.toString(),
            isRecurring = this.isRecurring,
            syncStatus = this.syncStatus.name.lowercase(),
            syncVersion = this.syncVersion,
            localId = this.localId,
            createdAt = this.createdAt.toEpochMilli(),
            updatedAt = this.updatedAt.toEpochMilli(),
            deletedAt = null
        )
    }
    
    /**
     * Convert IncomeDto to IncomeEntity
     */
    fun IncomeDto.toEntity(userId: String): IncomeEntity {
        return IncomeEntity(
            id = this.id,
            userId = userId,
            amount = this.amount,
            source = this.source,
            description = this.description,
            notes = this.notes,
            date = this.date,
            isRecurring = false, // isRecurring not in DTO yet
            syncStatus = this.syncStatus,
            syncVersion = this.syncVersion,
            localId = null,
            createdAt = Instant.parse(this.createdAt).toEpochMilli(),
            updatedAt = Instant.parse(this.updatedAt).toEpochMilli(),
            deletedAt = null
        )
    }
}
