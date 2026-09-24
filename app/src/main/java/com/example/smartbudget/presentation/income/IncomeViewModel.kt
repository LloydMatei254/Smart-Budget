package com.example.smartbudget.presentation.income

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.domain.model.IncomeSource
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
 * ViewModel for Income list screen
 */
@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository
) : BaseViewModel() {
    
    // Income state
    private val _incomes = MutableStateFlow<UiState<List<Income>>>(UiState.Loading)
    val incomes: StateFlow<UiState<List<Income>>> = _incomes.asStateFlow()
    
    // Filter state
    private val _selectedSource = MutableStateFlow<IncomeSource?>(null)
    val selectedSource: StateFlow<IncomeSource?> = _selectedSource.asStateFlow()
    
    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange: StateFlow<DateRange?> = _dateRange.asStateFlow()
    
    private val _sortBy = MutableStateFlow(SortOption.DATE_DESC)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Filtered and sorted income
    val filteredIncome: StateFlow<List<Income>> = combine(
        _incomes,
        _selectedSource,
        _dateRange,
        _searchQuery,
        _sortBy
    ) { incomesState, source, dateRange, query, sortOption ->
        if (incomesState is UiState.Success) {
            var filtered = incomesState.data
            
            // Filter by source
            if (source != null) {
                filtered = filtered.filter { it.source == source }
            }
            
            // Filter by date range
            if (dateRange != null) {
                filtered = filtered.filter { income ->
                    !income.date.isBefore(dateRange.startDate) && 
                    !income.date.isAfter(dateRange.endDate)
                }
            }
            
            // Filter by search query
            if (query.isNotEmpty()) {
                filtered = filtered.filter { income ->
                    income.description.contains(query, ignoreCase = true) ||
                    income.notes?.contains(query, ignoreCase = true) == true
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
    
    // Total income amount
    val totalIncome: StateFlow<BigDecimal> = filteredIncome.map { incomes ->
        incomes.fold(BigDecimal.ZERO) { acc, income -> acc + income.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)
    
    // Available income sources
    val availableSources = IncomeSource.values().toList()
    
    init {
        loadIncome()
    }
    
    /**
     * Load all income
     */
    fun loadIncome() {
        viewModelScope.launch {
            try {
                _incomes.value = UiState.Loading
                
                // Get current month by default
                val now = LocalDate.now()
                val startOfMonth = now.withDayOfMonth(1)
                val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
                
                incomeRepository.getIncomeForDateRangeFlow(startOfMonth, endOfMonth)
                    .catch { error ->
                        _incomes.value = UiState.Error(
                            error.message ?: "Failed to load income",
                            error
                        )
                        Timber.e(error, "Failed to load income")
                    }
                    .collect { incomeList ->
                        _incomes.value = UiState.Success(incomeList)
                        Timber.d("Loaded ${incomeList.size} income entries")
                    }
            } catch (e: Exception) {
                _incomes.value = UiState.Error(
                    e.message ?: "Unknown error",
                    e
                )
                Timber.e(e, "Exception loading income")
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
     * Filter by source
     */
    fun filterBySource(source: IncomeSource?) {
        _selectedSource.value = source
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
        _selectedSource.value = null
        _dateRange.value = null
        _searchQuery.value = ""
    }
    
    /**
     * Delete income
     */
    fun deleteIncome(income: Income) {
        sendEvent(
            com.example.smartbudget.presentation.common.UiEvent.ShowConfirmation(
                title = "Delete Income",
                message = "Are you sure you want to delete this income entry? This action cannot be undone.",
                confirmLabel = "Delete",
                onConfirm = {
                    performDeleteIncome(income)
                }
            )
        )
    }
    
    private fun performDeleteIncome(income: Income) {
        launchWithLoading {
            val result = incomeRepository.deleteIncome(income.id)
            
            result.onSuccess {
                showToast("Income deleted successfully")
            }
            
            result.onFailure { error ->
                showError("Failed to delete income: ${error.message}")
                Timber.e(error, "Failed to delete income")
            }
        }
    }
    
    /**
     * Refresh income from server
     */
    fun refresh() {
        launchSilent {
            incomeRepository.refreshIncome()
                .onSuccess {
                    showToast("Income synced")
                }
                .onFailure { error ->
                    showSnackbar("Sync failed: ${error.message}")
                }
        }
    }
    
    /**
     * Navigate to add income
     */
    fun navigateToAddIncome() {
        navigate("add_income")
    }
    
    /**
     * Navigate to income detail
     */
    fun navigateToIncomeDetail(incomeId: String) {
        navigate("income_detail/$incomeId")
    }
    
    /**
     * Navigate to edit income
     */
    fun navigateToEditIncome(incomeId: String) {
        navigate("edit_income/$incomeId")
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
 * Sort options for income
 */
enum class SortOption {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}
