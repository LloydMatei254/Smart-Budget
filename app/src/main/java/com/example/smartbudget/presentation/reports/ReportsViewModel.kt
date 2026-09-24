package com.example.smartbudget.presentation.reports

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.CategorySpending
import com.example.smartbudget.domain.model.FinancialSummary
import com.example.smartbudget.domain.repository.ReportRepository
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Reports screen with charts
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : BaseViewModel() {
    
    // Financial summary
    private val _financialSummary = MutableStateFlow<UiState<FinancialSummary>>(UiState.Loading)
    val financialSummary: StateFlow<UiState<FinancialSummary>> = _financialSummary.asStateFlow()
    
    // Category expenses for pie chart
    private val _categoryExpenses = MutableStateFlow<UiState<List<CategorySpending>>>(UiState.Loading)
    val categoryExpenses: StateFlow<UiState<List<CategorySpending>>> = _categoryExpenses.asStateFlow()
    
    // Period selection
    private val _selectedPeriod = MutableStateFlow(Period.THIS_MONTH)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()
    
    init {
        loadReports()
    }
    
    /**
     * Load all reports
     */
    fun loadReports() {
        loadFinancialSummary()
        loadCategoryExpenses()
    }
    
    /**
     * Load financial summary
     */
    private fun loadFinancialSummary() {
        viewModelScope.launch {
            try {
                _financialSummary.value = UiState.Loading
                
                val period = getPeriodDates()
                
                val result = reportRepository.getFinancialSummary(
                    startDate = period.first,
                    endDate = period.second
                )
                
                result.onSuccess { summary ->
                    _financialSummary.value = UiState.Success(summary)
                }
                
                result.onFailure { error ->
                    _financialSummary.value = UiState.Error(
                        error.message ?: "Failed to load summary",
                        error
                    )
                    Timber.e(error, "Failed to load financial summary")
                }
            } catch (e: Exception) {
                _financialSummary.value = UiState.Error(e.message ?: "Unknown error", e)
            }
        }
    }
    
    /**
     * Load category expenses
     */
    private fun loadCategoryExpenses() {
        viewModelScope.launch {
            try {
                _categoryExpenses.value = UiState.Loading
                
                val period = getPeriodDates()
                
                val result = reportRepository.getSpendingByCategory(
                    startDate = period.first,
                    endDate = period.second
                )
                
                result.onSuccess { categories ->
                    _categoryExpenses.value = UiState.Success(categories)
                }
                
                result.onFailure { error ->
                    _categoryExpenses.value = UiState.Error(
                        error.message ?: "Failed to load categories",
                        error
                    )
                    Timber.e(error, "Failed to load category expenses")
                }
            } catch (e: Exception) {
                _categoryExpenses.value = UiState.Error(e.message ?: "Unknown error", e)
            }
        }
    }
    
    /**
     * Change period
     */
    fun changePeriod(period: Period) {
        _selectedPeriod.value = period
        loadReports()
    }
    
    /**
     * Get start and end dates for selected period
     */
    private fun getPeriodDates(): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        
        return when (_selectedPeriod.value) {
            Period.THIS_MONTH -> {
                val start = now.withDayOfMonth(1)
                val end = now.withDayOfMonth(now.lengthOfMonth())
                start to end
            }
            Period.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1)
                val end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
                start to end
            }
            Period.LAST_3_MONTHS -> {
                val start = now.minusMonths(3)
                start to now
            }
            Period.THIS_YEAR -> {
                val start = now.withDayOfYear(1)
                start to now
            }
        }
    }
    
    /**
     * Navigate back
     */
    override fun navigateBack() {
        super.navigateBack()
    }
}

enum class Period {
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    THIS_YEAR
}
