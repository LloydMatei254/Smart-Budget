package com.example.smartbudget.presentation.dashboard

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Expense
import com.example.smartbudget.domain.model.FinancialSummary
import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.domain.model.Transaction
import com.example.smartbudget.domain.model.User
import com.example.smartbudget.domain.repository.ExpenseRepository
import com.example.smartbudget.domain.repository.IncomeRepository
import com.example.smartbudget.domain.repository.ReportRepository
import com.example.smartbudget.domain.usecase.auth.GetCurrentUserUseCase
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import com.example.smartbudget.utils.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Dashboard screen
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val reportRepository: ReportRepository
) : BaseViewModel() {
    
    // User state
    private val _user = MutableStateFlow<UiState<User>>(UiState.Loading)
    val user: StateFlow<UiState<User>> = _user.asStateFlow()
    
    // Financial summary state
    private val _financialSummary = MutableStateFlow<UiState<FinancialSummary>>(UiState.Loading)
    val financialSummary: StateFlow<UiState<FinancialSummary>> = _financialSummary.asStateFlow()
    
    // Recent transactions
    private val _recentTransactions = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val recentTransactions: StateFlow<UiState<List<Transaction>>> = _recentTransactions.asStateFlow()
    
    // Refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Balance display
    private val _balanceVisible = MutableStateFlow(true)
    val balanceVisible: StateFlow<Boolean> = _balanceVisible.asStateFlow()
    
    // Formatted values for display
    val formattedBalance: StateFlow<String> = _financialSummary.map { state ->
        if (state is UiState.Success) {
            CurrencyUtils.formatAmount(state.data.balance)
        } else {
            "$0.00"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "$0.00")
    
    val formattedTotalIncome: StateFlow<String> = _financialSummary.map { state ->
        if (state is UiState.Success) {
            CurrencyUtils.formatAmount(state.data.totalIncome)
        } else {
            "$0.00"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "$0.00")
    
    val formattedTotalExpenses: StateFlow<String> = _financialSummary.map { state ->
        if (state is UiState.Success) {
            CurrencyUtils.formatAmount(state.data.totalExpenses)
        } else {
            "$0.00"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "$0.00")
    
    // Balance change percentage
    val balanceChangePercentage: StateFlow<String> = _financialSummary.map { state ->
        if (state is UiState.Success) {
            // Calculate savings rate as (income - expenses) / income * 100
            val savingsRate = if (state.data.totalIncome > BigDecimal.ZERO) {
                ((state.data.totalIncome - state.data.totalExpenses) / state.data.totalIncome * BigDecimal(100)).toDouble()
            } else {
                0.0
            }
            val sign = if (savingsRate >= 0) "+" else ""
            "$sign${String.format("%.1f", savingsRate)}%"
        } else {
            "+0.0%"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "+0.0%")
    
    val balanceChangePositive: StateFlow<Boolean> = _financialSummary.map { state ->
        if (state is UiState.Success) {
            // Positive if we have income greater than expenses
            state.data.totalIncome >= state.data.totalExpenses
        } else {
            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    
    init {
        loadDashboardData()
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        loadUser()
        loadFinancialSummary()
        loadRecentTransactions()
    }
    
    /**
     * Load current user
     */
    private fun loadUser() {
        executeWithState(_user) {
            getCurrentUserUseCase().mapCatching { user ->
                user ?: throw IllegalStateException("User not logged in")
            }
        }
    }
    
    /**
     * Load financial summary for current month
     */
    private fun loadFinancialSummary() {
        viewModelScope.launch {
            try {
                _financialSummary.value = UiState.Loading
                
                val now = LocalDate.now()
                val startOfMonth = now.withDayOfMonth(1)
                val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
                
                val result = reportRepository.getFinancialSummary(
                    startDate = startOfMonth,
                    endDate = endOfMonth
                )
                
                result.onSuccess { summary ->
                    _financialSummary.value = UiState.Success(summary)
                    Timber.d("Financial summary loaded: balance=${summary.balance}")
                }
                
                result.onFailure { error ->
                    _financialSummary.value = UiState.Error(
                        error.message ?: "Failed to load financial summary",
                        error
                    )
                    Timber.e(error, "Failed to load financial summary")
                }
            } catch (e: Exception) {
                _financialSummary.value = UiState.Error(
                    e.message ?: "Unknown error",
                    e
                )
                Timber.e(e, "Exception loading financial summary")
            }
        }
    }
    
    /**
     * Load recent transactions (last 5)
     */
    private fun loadRecentTransactions() {
        viewModelScope.launch {
            try {
                _recentTransactions.value = UiState.Loading
                
                // Get recent expenses and income
                val now = LocalDate.now()
                val thirtyDaysAgo = now.minusDays(30)
                
                val expenses = expenseRepository.getExpensesForDateRange(
                    startDate = thirtyDaysAgo,
                    endDate = now
                )
                
                val income = incomeRepository.getIncomeForDateRange(
                    startDate = thirtyDaysAgo,
                    endDate = now
                )
                
                // Convert to transactions and combine
                val expenseTransactions = expenses.map { expense ->
                    // We need ExpenseWithDetails for proper Transaction conversion
                    // For now, create basic transaction
                    Transaction.ExpenseTransaction(
                        id = expense.id,
                        amount = expense.amount,
                        description = expense.description,
                        date = expense.date,
                        categoryOrSource = expense.categoryId, // Will be replaced with actual category name
                        color = "#E74C3C",
                        icon = "shopping_cart",
                        paymentMethod = expense.paymentMethodId,
                        notes = expense.notes
                    )
                }
                
                val incomeTransactions = income.map { inc ->
                    Transaction.IncomeTransaction(
                        id = inc.id,
                        amount = inc.amount,
                        description = inc.description,
                        date = inc.date,
                        categoryOrSource = inc.source.displayName,
                        color = "#27AE60",
                        icon = "trending_up"
                    )
                }
                
                // Combine and sort by date (newest first)
                val allTransactions = (expenseTransactions + incomeTransactions)
                    .sortedByDescending { it.date }
                    .take(5)
                
                _recentTransactions.value = UiState.Success(allTransactions)
                Timber.d("Loaded ${allTransactions.size} recent transactions")
            } catch (e: Exception) {
                _recentTransactions.value = UiState.Error(
                    e.message ?: "Failed to load transactions",
                    e
                )
                Timber.e(e, "Failed to load recent transactions")
            }
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            try {
                // Sync with server
                expenseRepository.refreshExpenses()
                incomeRepository.refreshIncome()
                
                // Reload data
                loadDashboardData()
            } catch (e: Exception) {
                showSnackbar("Failed to refresh: ${e.message}")
                Timber.e(e, "Refresh failed")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Toggle balance visibility
     */
    fun toggleBalanceVisibility() {
        _balanceVisible.value = !_balanceVisible.value
    }
    
    /**
     * Navigate to add expense
     */
    fun navigateToAddExpense() {
        navigate("add_expense")
    }
    
    /**
     * Navigate to add income
     */
    fun navigateToAddIncome() {
        navigate("add_income")
    }
    
    /**
     * Navigate to transactions
     */
    fun navigateToTransactions() {
        navigate("transactions")
    }
    
    /**
     * Navigate to transaction detail
     */
    fun navigateToTransactionDetail(transactionId: String, isExpense: Boolean) {
        if (isExpense) {
            navigate("expense_detail/$transactionId")
        } else {
            navigate("income_detail/$transactionId")
        }
    }
    
    /**
     * Navigate to reports
     */
    fun navigateToReports() {
        navigate("reports")
    }
    
    /**
     * Navigate to settings
     */
    fun navigateToSettings() {
        navigate("settings")
    }
    
    /**
     * Get greeting based on time of day
     */
    fun getGreeting(): String {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 0..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
