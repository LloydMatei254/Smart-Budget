package com.example.smartbudget.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.smartbudget.data.local.dao.CategoryDao
import com.example.smartbudget.data.local.dao.ExpenseDao
import com.example.smartbudget.data.local.dao.IncomeDao
import com.example.smartbudget.data.local.dao.PaymentMethodDao
import com.example.smartbudget.data.local.dao.UserDao
import com.example.smartbudget.data.local.entities.CategoryEntity
import com.example.smartbudget.data.local.entities.ExpenseEntity
import com.example.smartbudget.data.local.entities.IncomeEntity
import com.example.smartbudget.data.local.entities.PaymentMethodEntity
import com.example.smartbudget.data.local.entities.UserEntity

/**
 * Room database for Smart Budget application
 * 
 * This database provides local caching for offline-first functionality.
 * All user data is stored locally and synchronized with the backend.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        PaymentMethodEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SmartBudgetDatabase : RoomDatabase() {
    
    /**
     * Get UserDao
     */
    abstract fun userDao(): UserDao
    
    /**
     * Get CategoryDao
     */
    abstract fun categoryDao(): CategoryDao
    
    /**
     * Get ExpenseDao
     */
    abstract fun expenseDao(): ExpenseDao
    
    /**
     * Get IncomeDao
     */
    abstract fun incomeDao(): IncomeDao
    
    /**
     * Get PaymentMethodDao
     */
    abstract fun paymentMethodDao(): PaymentMethodDao
    
    companion object {
        const val DATABASE_NAME = "smart_budget_database"
    }
}
