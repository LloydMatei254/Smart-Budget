package com.example.smartbudget.di

import android.content.Context
import androidx.room.Room
import com.example.smartbudget.data.local.database.SmartBudgetDatabase
import com.example.smartbudget.data.local.dao.CategoryDao
import com.example.smartbudget.data.local.dao.ExpenseDao
import com.example.smartbudget.data.local.dao.IncomeDao
import com.example.smartbudget.data.local.dao.PaymentMethodDao
import com.example.smartbudget.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provide SmartBudgetDatabase instance
     */
    @Provides
    @Singleton
    fun provideSmartBudgetDatabase(
        @ApplicationContext context: Context
    ): SmartBudgetDatabase {
        return Room.databaseBuilder(
            context,
            SmartBudgetDatabase::class.java,
            "smart_budget_database"
        )
            .fallbackToDestructiveMigration() // TODO: Remove in production, implement proper migrations
            .build()
    }

    /**
     * Provide UserDao
     */
    @Provides
    @Singleton
    fun provideUserDao(database: SmartBudgetDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Provide ExpenseDao
     */
    @Provides
    @Singleton
    fun provideExpenseDao(database: SmartBudgetDatabase): ExpenseDao {
        return database.expenseDao()
    }

    /**
     * Provide IncomeDao
     */
    @Provides
    @Singleton
    fun provideIncomeDao(database: SmartBudgetDatabase): IncomeDao {
        return database.incomeDao()
    }

    /**
     * Provide CategoryDao
     */
    @Provides
    @Singleton
    fun provideCategoryDao(database: SmartBudgetDatabase): CategoryDao {
        return database.categoryDao()
    }

    /**
     * Provide PaymentMethodDao
     */
    @Provides
    @Singleton
    fun providePaymentMethodDao(database: SmartBudgetDatabase): PaymentMethodDao {
        return database.paymentMethodDao()
    }
}
