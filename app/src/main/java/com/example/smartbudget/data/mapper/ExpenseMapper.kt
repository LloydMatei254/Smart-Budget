package com.example.smartbudget.data.mapper

import com.example.smartbudget.data.local.entities.ExpenseEntity
import com.example.smartbudget.data.local.relations.ExpenseWithRelations
import com.example.smartbudget.data.mapper.CategoryMapper.toDomain
import com.example.smartbudget.data.mapper.PaymentMethodMapper.toDomain
import com.example.smartbudget.data.remote.dto.response.ExpenseDto
import com.example.smartbudget.domain.model.Expense
import com.example.smartbudget.domain.model.ExpenseWithDetails
import com.example.smartbudget.domain.model.SyncStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Mapper for converting between Expense representations
 */
object ExpenseMapper {
    
    /**
     * Convert ExpenseDto (API response) to Expense (domain model)
     */
    fun ExpenseDto.toDomain(): Expense {
        return Expense(
            id = this.id,
            userId = "", // userId not included in API response
            categoryId = this.category.id,
            paymentMethodId = this.paymentMethod?.id,
            amount = BigDecimal(this.amount),
            description = this.description,
            notes = this.notes,
            date = LocalDate.parse(this.date),
            receiptPhotoUrl = null, // Receipt URL not in DTO yet
            syncStatus = SyncStatus.fromString(this.syncStatus),
            syncVersion = this.syncVersion,
            localId = null,
            createdAt = Instant.parse(this.createdAt),
            updatedAt = Instant.parse(this.updatedAt)
        )
    }
    
    /**
     * Convert ExpenseEntity (Room) to Expense (domain model)
     */
    fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = this.id,
            userId = this.userId,
            categoryId = this.categoryId,
            paymentMethodId = this.paymentMethodId,
            amount = BigDecimal(this.amount),
            description = this.description,
            notes = this.notes,
            date = LocalDate.parse(this.date),
            receiptPhotoUrl = this.receiptPhotoUrl,
            syncStatus = SyncStatus.fromString(this.syncStatus),
            syncVersion = this.syncVersion,
            localId = this.localId,
            createdAt = Instant.ofEpochMilli(this.createdAt),
            updatedAt = Instant.ofEpochMilli(this.updatedAt)
        )
    }
    
    /**
     * Convert Expense (domain model) to ExpenseEntity (Room)
     */
    fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = this.id,
            userId = this.userId,
            categoryId = this.categoryId,
            paymentMethodId = this.paymentMethodId,
            amount = this.amount.toPlainString(),
            description = this.description,
            notes = this.notes,
            date = this.date.toString(),
            receiptPhotoUrl = this.receiptPhotoUrl,
            syncStatus = this.syncStatus.name.lowercase(),
            syncVersion = this.syncVersion,
            localId = this.localId,
            createdAt = this.createdAt.toEpochMilli(),
            updatedAt = this.updatedAt.toEpochMilli(),
            deletedAt = null
        )
    }
    
    /**
     * Convert ExpenseDto to ExpenseEntity
     */
    fun ExpenseDto.toEntity(userId: String): ExpenseEntity {
        return ExpenseEntity(
            id = this.id,
            userId = userId,
            categoryId = this.category.id,
            paymentMethodId = this.paymentMethod?.id,
            amount = this.amount,
            description = this.description,
            notes = this.notes,
            date = this.date,
            receiptPhotoUrl = null, // Receipt URL not in DTO yet
            syncStatus = this.syncStatus,
            syncVersion = this.syncVersion,
            localId = null,
            createdAt = Instant.parse(this.createdAt).toEpochMilli(),
            updatedAt = Instant.parse(this.updatedAt).toEpochMilli(),
            deletedAt = null
        )
    }
    
    /**
     * Convert ExpenseWithRelations to ExpenseWithDetails (domain model)
     */
    fun ExpenseWithRelations.toDomain(): ExpenseWithDetails {
        return ExpenseWithDetails(
            expense = this.expense.toDomain(),
            category = this.category?.toDomain() 
                ?: throw IllegalStateException("Expense must have a category"),
            paymentMethod = this.paymentMethod?.toDomain()
        )
    }
}
