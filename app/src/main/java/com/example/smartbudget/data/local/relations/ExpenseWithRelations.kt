package com.example.smartbudget.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.smartbudget.data.local.entities.CategoryEntity
import com.example.smartbudget.data.local.entities.ExpenseEntity
import com.example.smartbudget.data.local.entities.PaymentMethodEntity

/**
 * Room relation class for Expense with its related entities
 */
data class ExpenseWithRelations(
    @Embedded
    val expense: ExpenseEntity,
    
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,
    
    @Relation(
        parentColumn = "paymentMethodId",
        entityColumn = "id"
    )
    val paymentMethod: PaymentMethodEntity?
)
