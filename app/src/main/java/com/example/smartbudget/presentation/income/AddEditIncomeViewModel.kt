package com.example.smartbudget.presentation.income

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Income
import com.example.smartbudget.domain.model.IncomeSource
import com.example.smartbudget.domain.repository.IncomeRepository
import com.example.smartbudget.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Add/Edit Income screen
 */
@HiltViewModel
class AddEditIncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    
    // Income ID for editing (null for new income)
    private val incomeId: String? = savedStateHandle["incomeId"]
    val isEditing: Boolean = incomeId != null
    
    // Existing income state (for editing)
    private val _existingIncome = MutableStateFlow<Income?>(null)
    val existingIncome: StateFlow<Income?> = _existingIncome.asStateFlow()
    
    // Form state
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()
    
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()
    
    private val _selectedSource = MutableStateFlow<IncomeSource>(IncomeSource.SALARY)
    val selectedSource: StateFlow<IncomeSource> = _selectedSource.asStateFlow()
    
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()
    
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()
    
    private val _isRecurring = MutableStateFlow(false)
    val isRecurring: StateFlow<Boolean> = _isRecurring.asStateFlow()
    
    // Validation errors
    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()
    
    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()
    
    // Form validity
    val isFormValid: StateFlow<Boolean> = combine(
        _amount,
        _description
    ) { amount, description ->
        amount.isNotEmpty() && 
        amount.toDoubleOrNull() != null && 
        amount.toDouble() > 0 &&
        description.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // Available income sources
    val availableSources = IncomeSource.values().toList()
    
    init {
        if (isEditing && incomeId != null) {
            loadIncome(incomeId)
        }
    }
    
    /**
     * Load existing income for editing
     */
    private fun loadIncome(id: String) {
        launchWithLoading {
            val income = incomeRepository.getIncomeById(id)
            
            if (income != null) {
                _existingIncome.value = income
                
                // Populate form
                _amount.value = income.amount.toPlainString()
                _description.value = income.description
                _selectedSource.value = income.source
                _date.value = income.date
                _notes.value = income.notes ?: ""
                _isRecurring.value = income.isRecurring
            } else {
                showError("Income not found")
            }
        }
    }
    
    /**
     * Update amount
     */
    fun onAmountChanged(value: String) {
        _amount.value = value
        validateAmount()
    }
    
    private fun validateAmount() {
        _amountError.value = when {
            _amount.value.isEmpty() -> "Amount is required"
            _amount.value.toDoubleOrNull() == null -> "Invalid amount"
            _amount.value.toDouble() <= 0 -> "Amount must be greater than 0"
            else -> null
        }
    }
    
    /**
     * Update description
     */
    fun onDescriptionChanged(value: String) {
        _description.value = value
        validateDescription()
    }
    
    private fun validateDescription() {
        _descriptionError.value = when {
            _description.value.isEmpty() -> "Description is required"
            _description.value.length < 3 -> "Description too short"
            else -> null
        }
    }
    
    /**
     * Select income source
     */
    fun selectSource(source: IncomeSource) {
        _selectedSource.value = source
    }
    
    /**
     * Update date
     */
    fun onDateChanged(date: LocalDate) {
        _date.value = date
    }
    
    /**
     * Update notes
     */
    fun onNotesChanged(value: String) {
        _notes.value = value
    }
    
    /**
     * Toggle recurring
     */
    fun toggleRecurring(isRecurring: Boolean) {
        _isRecurring.value = isRecurring
    }
    
    /**
     * Save income
     */
    fun saveIncome() {
        // Validate all fields
        validateAmount()
        validateDescription()
        
        if (!isFormValid.value) {
            showSnackbar("Please fix errors before saving")
            return
        }
        
        launchWithLoading {
            try {
                val amountValue = BigDecimal(_amount.value)
                val source = _selectedSource.value
                
                if (isEditing && incomeId != null) {
                    // Update existing income
                    val existing = _existingIncome.value
                    if (existing != null) {
                        val updated = existing.copy(
                            amount = amountValue,
                            description = _description.value.trim(),
                            source = source,
                            date = _date.value,
                            notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                            isRecurring = _isRecurring.value,
                            updatedAt = Instant.now()
                        )
                        
                        val result = incomeRepository.updateIncome(updated)
                        
                        result.onSuccess {
                            showToast("Income updated successfully")
                            navigateBack()
                        }
                        
                        result.onFailure { error ->
                            showError("Failed to update income: ${error.message}")
                            Timber.e(error, "Failed to update income")
                        }
                    }
                } else {
                    // Create new income
                    val newIncome = Income(
                        id = "", // Will be generated
                        userId = "", // Will be set by repository
                        amount = amountValue,
                        description = _description.value.trim(),
                        source = source,
                        date = _date.value,
                        notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                        isRecurring = _isRecurring.value,
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                    
                    val result = incomeRepository.createIncome(newIncome)
                    
                    result.onSuccess {
                        showToast("Income created successfully")
                        navigateBack()
                    }
                    
                    result.onFailure { error ->
                        showError("Failed to create income: ${error.message}")
                        Timber.e(error, "Failed to create income")
                    }
                }
            } catch (e: Exception) {
                showError("Error saving income: ${e.message}")
                Timber.e(e, "Exception saving income")
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
