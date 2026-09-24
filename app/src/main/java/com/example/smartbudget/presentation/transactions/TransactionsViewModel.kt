package com.example.smartbudget.presentation.transactions

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Transaction
import com.example.smartbudget.domain.repository.ExpenseRepository
import com.example.smartbudget.domain.repository.IncomeRepository
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for unified Transactions (Expenses + Income) screen
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : BaseViewModel() {
    
    // Filter state
    private val _transactionType = MutableStateFlow(TransactionType.ALL)
    val transactionType: StateFlow<TransactionType> = _transactionType.asStateFlow()
    
    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange: StateFlow<DateRange?> = _dateRange.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Combined transactions
    private val _transactions = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val transactions: StateFlow<UiState<List<Transaction>>> = _transactions.asStateFlow()
    
    // Filtered transactions
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _transactions,
        _transactionType,
        _searchQuery
    ) { transactionsState, type, query ->
        if (transactionsState is UiState.Success) {
            var filtered = transactionsState.data
            
            // Filter by type
            filtered = when (type) {
                TransactionType.ALL -> filtered
                TransactionType.EXPENSES -> filtered.filterIsInstance<Transaction.ExpenseTransaction>()
                TransactionType.INCOME -> filtered.filterIsInstance<Transaction.IncomeTransaction>()
            }
            
            // Filter by search
            if (query.isNotEmpty()) {
                filtered = filtered.filter { transaction ->
                    transaction.description.contains(query, ignoreCase = true) ||
                    transaction.categoryOrSource.contains(query, ignoreCase = true)
                }
            }
            
            filtered
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Statistics
    val totalIncome: StateFlow<BigDecimal> = filteredTransactions.map { transactions ->
        transactions.filterIsInstance<Transaction.IncomeTransaction>()
            .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)
    
    val totalExpenses: StateFlow<BigDecimal> = filteredTransactions.map { transactions ->
        transactions.filterIsInstance<Transaction.ExpenseTransaction>()
            .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)
    
    val balance: StateFlow<BigDecimal> = combine(totalIncome, totalExpenses) { income, expenses ->
        income - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)
    
    init {
        loadTransactions()
    }
    
    /**
     * Load all transactions (expenses + income)
     */
    fun loadTransactions() {
        viewModelScope.launch {
            try {
                _transactions.value = UiState.Loading
                
                val now = LocalDate.now()
                val startDate = _dateRange.value?.startDate ?: now.minusMonths(3)
                val endDate = _dateRange.value?.endDate ?: now
                
                // Combine expenses and income flows
                combine(
                    expenseRepository.getExpensesForDateRangeFlow(startDate, endDate),
                    incomeRepository.getIncomeForDateRangeFlow(startDate, endDate)
                ) { expenses, income ->
                    // Convert to transactions
                    val expenseTransactions = expenses.map { expense ->
                        Transaction.ExpenseTransaction(
                            id = expense.id,
                            amount = expense.amount,
                            description = expense.description,
                            date = expense.date,
                            categoryOrSource = expense.categoryId,
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
                    (expenseTransactions + incomeTransactions).sortedByDescending { it.date }
                }
                .catch { error ->
                    _transactions.value = UiState.Error(
                        error.message ?: "Failed to load transactions",
                        error
                    )
                    Timber.e(error, "Failed to load transactions")
                }
                .collect { transactionList ->
                    _transactions.value = UiState.Success(transactionList)
                    Timber.d("Loaded ${transactionList.size} transactions")
                }
            } catch (e: Exception) {
                _transactions.value = UiState.Error(e.message ?: "Unknown error", e)
                Timber.e(e, "Exception loading transactions")
            }
        }
    }
    
    /**
     * Update search query
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Filter by transaction type
     */
    fun filterByType(type: TransactionType) {
        _transactionType.value = type
    }
    
    /**
     * Filter by date range
     */
    fun filterByDateRange(startDate: LocalDate, endDate: LocalDate) {
        _dateRange.value = DateRange(startDate, endDate)
        loadTransactions() // Reload with new date range
    }
    
    /**
     * Clear filters
     */
    fun clearFilters() {
        _transactionType.value = TransactionType.ALL
        _searchQuery.value = ""
    }
    
    /**
     * Navigate to transaction detail
     */
    fun navigateToTransactionDetail(transaction: Transaction) {
        when (transaction) {
            is Transaction.ExpenseTransaction -> navigate("expense_detail/${transaction.id}")
            is Transaction.IncomeTransaction -> navigate("income_detail/${transaction.id}")
        }
    }
    
    /**
     * Navigate back
     */
    override fun navigateBack() {
        super.navigateBack()
    }
}

enum class TransactionType {
    ALL, EXPENSES, INCOME
}

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)
