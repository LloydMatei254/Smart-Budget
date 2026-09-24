package com.example.smartbudget.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Unified transaction model for displaying both income and expenses together.
 * Used in the transaction history screen.
 */
sealed class Transaction {
    abstract val id: String
    abstract val amount: BigDecimal
    abstract val description: String
    abstract val date: LocalDate
    abstract val categoryOrSource: String
    abstract val color: String
    abstract val icon: String?

    data class ExpenseTransaction(
        override val id: String,
        override val amount: BigDecimal,
        override val description: String,
        override val date: LocalDate,
        override val categoryOrSource: String,
        override val color: String,
        override val icon: String?,
        val paymentMethod: String?,
        val notes: String?
    ) : Transaction() {
        val isExpense: Boolean = true
    }

    data class IncomeTransaction(
        override val id: String,
        override val amount: BigDecimal,
        override val description: String,
        override val date: LocalDate,
        override val categoryOrSource: String,
        override val color: String = "#27AE60", // Green for income
        override val icon: String? = "trending_up"
    ) : Transaction() {
        val isIncome: Boolean = true
    }

    companion object {
        fun fromExpense(expense: ExpenseWithDetails): Transaction {
            return ExpenseTransaction(
                id = expense.id,
                amount = expense.amount,
                description = expense.description,
                date = expense.date,
                categoryOrSource = expense.categoryName,
                color = expense.categoryColor,
                icon = expense.categoryIcon,
                paymentMethod = expense.paymentMethodName,
                notes = expense.expense.notes
            )
        }

        fun fromIncome(income: Income): Transaction {
            return IncomeTransaction(
                id = income.id,
                amount = income.amount,
                description = income.description,
                date = income.date,
                categoryOrSource = income.source.displayName
            )
        }
    }
}
