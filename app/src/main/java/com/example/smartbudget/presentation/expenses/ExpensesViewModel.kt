package com.example.smartbudget.presentation.expenses

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Category
import com.example.smartbudget.domain.model.Expense
import com.example.smartbudget.domain.model.PaymentMethod
import com.example.smartbudget.domain.repository.CategoryRepository
import com.example.smartbudget.domain.repository.ExpenseRepository
import com.example.smartbudget.domain.repository.PaymentMethodRepository
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
 * ViewModel for Expenses list screen
 */
@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : BaseViewModel() {
    
    // Expenses state
    private val _expenses = MutableStateFlow<UiState<List<Expense>>>(UiState.Loading)
    val expenses: StateFlow<UiState<List<Expense>>> = _expenses.asStateFlow()
    
    // Categories for filtering
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    // Filter state
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()
    
    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange: StateFlow<DateRange?> = _dateRange.asStateFlow()
    
    private val _sortBy = MutableStateFlow(SortOption.DATE_DESC)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Filtered and sorted expenses
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        _expenses,
        _selectedCategoryId,
        _dateRange,
        _searchQuery,
        _sortBy
    ) { expensesState, categoryId, dateRange, query, sortOption ->
        if (expensesState is UiState.Success) {
            var filtered = expensesState.data
            
            // Filter by category
            if (categoryId != null) {
                filtered = filtered.filter { it.categoryId == categoryId }
            }
            
            // Filter by date range
            if (dateRange != null) {
                filtered = filtered.filter { expense ->
                    !expense.date.isBefore(dateRange.startDate) && 
                    !expense.date.isAfter(dateRange.endDate)
                }
            }
            
            // Filter by search query
            if (query.isNotEmpty()) {
                filtered = filtered.filter { expense ->
                    expense.description.contains(query, ignoreCase = true) ||
                    expense.notes?.contains(query, ignoreCase = true) == true
                }
            }
            
            // Sort
            when (sortOption) {
                SortOption.DATE_DESC -> filtered.sortedByDescending { it.date }
                SortOption.DATE_ASC -> filtered.sortedBy { it.date }
                SortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
                SortOption.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Total expenses amount
    val totalExpenses: StateFlow<BigDecimal> = filteredExpenses.map { expenses ->
        expenses.fold(BigDecimal.ZERO) { acc, expense -> acc + expense.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)
    
    init {
        loadExpenses()
        loadCategories()
    }
    
    /**
     * Load all expenses
     */
    fun loadExpenses() {
        viewModelScope.launch {
            try {
                _expenses.value = UiState.Loading
                
                // Get current month by default
                val now = LocalDate.now()
                val startOfMonth = now.withDayOfMonth(1)
                val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
                
                expenseRepository.getExpensesForDateRangeFlow(startOfMonth, endOfMonth)
                    .catch { error ->
                        _expenses.value = UiState.Error(
                            error.message ?: "Failed to load expenses",
                            error
                        )
                        Timber.e(error, "Failed to load expenses")
                    }
                    .collect { expenseList ->
                        _expenses.value = UiState.Success(expenseList)
                        Timber.d("Loaded ${expenseList.size} expenses")
                    }
            } catch (e: Exception) {
                _expenses.value = UiState.Error(
                    e.message ?: "Unknown error",
                    e
                )
                Timber.e(e, "Exception loading expenses")
            }
        }
    }
    
    /**
     * Load categories for filtering
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesFlow()
                .catch { error ->
                    Timber.e(error, "Failed to load categories")
                }
                .collect { categoryList ->
                    _categories.value = categoryList
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
     * Filter by category
     */
    fun filterByCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }
    
    /**
     * Filter by date range
     */
    fun filterByDateRange(startDate: LocalDate, endDate: LocalDate) {
        _dateRange.value = DateRange(startDate, endDate)
    }
    
    /**
     * Clear date range filter
     */
    fun clearDateRangeFilter() {
        _dateRange.value = null
    }
    
    /**
     * Change sort option
     */
    fun changeSortOption(option: SortOption) {
        _sortBy.value = option
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        _selectedCategoryId.value = null
        _dateRange.value = null
        _searchQuery.value = ""
    }
    
    /**
     * Delete expense
     */
    fun deleteExpense(expense: Expense) {
        sendEvent(
            com.example.smartbudget.presentation.common.UiEvent.ShowConfirmation(
                title = "Delete Expense",
                message = "Are you sure you want to delete this expense? This action cannot be undone.",
                confirmLabel = "Delete",
                onConfirm = {
                    performDeleteExpense(expense)
                }
            )
        )
    }
    
    private fun performDeleteExpense(expense: Expense) {
        launchWithLoading {
            val result = expenseRepository.deleteExpense(expense.id)
            
            result.onSuccess {
                showToast("Expense deleted successfully")
            }
            
            result.onFailure { error ->
                showError("Failed to delete expense: ${error.message}")
                Timber.e(error, "Failed to delete expense")
            }
        }
    }
    
    /**
     * Refresh expenses from server
     */
    fun refresh() {
        launchSilent {
            expenseRepository.refreshExpenses()
                .onSuccess {
                    showToast("Expenses synced")
                }
                .onFailure { error ->
                    showSnackbar("Sync failed: ${error.message}")
                }
        }
    }
    
    /**
     * Navigate to add expense
     */
    fun navigateToAddExpense() {
        navigate("add_expense")
    }
    
    /**
     * Navigate to expense detail
     */
    fun navigateToExpenseDetail(expenseId: String) {
        navigate("expense_detail/$expenseId")
    }
    
    /**
     * Navigate to edit expense
     */
    fun navigateToEditExpense(expenseId: String) {
        navigate("edit_expense/$expenseId")
    }
    
    /**
     * Navigate back
     */
    override fun navigateBack() {
        super.navigateBack()
    }
}

/**
 * Date range for filtering
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * Sort options for expenses
 */
enum class SortOption {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}
