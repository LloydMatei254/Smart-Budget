package com.example.smartbudget.di

import com.example.smartbudget.data.local.dao.CategoryDao
import com.example.smartbudget.data.local.dao.ExpenseDao
import com.example.smartbudget.data.local.dao.IncomeDao
import com.example.smartbudget.data.local.dao.PaymentMethodDao
import com.example.smartbudget.data.local.dao.UserDao
import com.example.smartbudget.data.remote.api.AuthApi
import com.example.smartbudget.data.remote.api.CategoriesApi
import com.example.smartbudget.data.remote.api.ExpensesApi
import com.example.smartbudget.data.remote.api.IncomeApi
import com.example.smartbudget.data.remote.api.PaymentMethodsApi
import com.example.smartbudget.data.remote.api.ReportsApi
import com.example.smartbudget.data.remote.api.SyncApi
import com.example.smartbudget.data.repository.AuthRepositoryImpl
import com.example.smartbudget.data.repository.CategoryRepositoryImpl
import com.example.smartbudget.data.repository.ExpenseRepositoryImpl
import com.example.smartbudget.data.repository.IncomeRepositoryImpl
import com.example.smartbudget.data.repository.PaymentMethodRepositoryImpl
import com.example.smartbudget.data.repository.ReportRepositoryImpl
import com.example.smartbudget.data.repository.SyncRepositoryImpl
import com.example.smartbudget.domain.repository.AuthRepository
import com.example.smartbudget.domain.repository.CategoryRepository
import com.example.smartbudget.domain.repository.ExpenseRepository
import com.example.smartbudget.domain.repository.IncomeRepository
import com.example.smartbudget.domain.repository.PaymentMethodRepository
import com.example.smartbudget.domain.repository.ReportRepository
import com.example.smartbudget.domain.repository.SyncRepository
import com.example.smartbudget.utils.PreferencesManager
import com.example.smartbudget.utils.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provide AuthRepository implementation
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        userDao: UserDao,
        secureStorage: SecureStorage,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): AuthRepository {
        return AuthRepositoryImpl(
            authApi = authApi,
            userDao = userDao,
            secureStorage = secureStorage,
            preferencesManager = preferencesManager,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provide ExpenseRepository implementation
     */
    @Provides
    @Singleton
    fun provideExpenseRepository(
        expenseDao: ExpenseDao,
        expensesApi: ExpensesApi,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(
            expenseDao = expenseDao,
            expensesApi = expensesApi,
            preferencesManager = preferencesManager,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provide IncomeRepository implementation
     */
    @Provides
    @Singleton
    fun provideIncomeRepository(
        incomeDao: IncomeDao,
        incomeApi: IncomeApi,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): IncomeRepository {
        return IncomeRepositoryImpl(
            incomeDao = incomeDao,
            incomeApi = incomeApi,
            preferencesManager = preferencesManager,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provide CategoryRepository implementation
     */
    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        categoriesApi: CategoriesApi,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CategoryRepository {
        return CategoryRepositoryImpl(
            categoryDao = categoryDao,
            categoriesApi = categoriesApi,
            preferencesManager = preferencesManager,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provide PaymentMethodRepository implementation
     */
    @Provides
    @Singleton
    fun providePaymentMethodRepository(
        paymentMethodDao: PaymentMethodDao,
        paymentMethodsApi: PaymentMethodsApi,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PaymentMethodRepository {
        return PaymentMethodRepositoryImpl(
            paymentMethodDao = paymentMethodDao,
            paymentMethodsApi = paymentMethodsApi,
            preferencesManager = preferencesManager,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provide ReportRepository implementation
     */
    @Provides
    @Singleton
    fun provideReportRepository(
        reportsApi: ReportsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ReportRepository {
        return ReportRepositoryImpl(
            reportsApi = reportsApi,
            ioDispatcher = ioDispatcher
        )
    }

    /**
     * Provide SyncRepository implementation
     */
    @Provides
    @Singleton
    fun provideSyncRepository(
        syncApi: SyncApi,
        expenseDao: ExpenseDao,
        incomeDao: IncomeDao,
        categoryDao: CategoryDao,
        paymentMethodDao: PaymentMethodDao,
        preferencesManager: PreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SyncRepository {
        return SyncRepositoryImpl(
            syncApi = syncApi,
            expenseDao = expenseDao,
            incomeDao = incomeDao,
            categoryDao = categoryDao,
            paymentMethodDao = paymentMethodDao,
            preferencesManager = preferencesManager,
            ioDispatcher = ioDispatcher
        )
    }
}
