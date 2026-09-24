package com.example.smartbudget.presentation.expenses

import androidx.lifecycle.SavedStateHandle
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
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Add/Edit Expense screen
 */
@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    
    // Expense ID for editing (null for new expense)
    private val expenseId: String? = savedStateHandle["expenseId"]
    val isEditing: Boolean = expenseId != null
    
    // Existing expense state (for editing)
    private val _existingExpense = MutableStateFlow<Expense?>(null)
    val existingExpense: StateFlow<Expense?> = _existingExpense.asStateFlow()
    
    // Form state
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()
    
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    
    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    val selectedPaymentMethod: StateFlow<PaymentMethod?> = _selectedPaymentMethod.asStateFlow()
    
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()
    
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()
    
    private val _receiptPhotoUri = MutableStateFlow<String?>(null)
    val receiptPhotoUri: StateFlow<String?> = _receiptPhotoUri.asStateFlow()
    
    // Available categories and payment methods
    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()
    
    private val _paymentMethods = MutableStateFlow<UiState<List<PaymentMethod>>>(UiState.Loading)
    val paymentMethods: StateFlow<UiState<List<PaymentMethod>>> = _paymentMethods.asStateFlow()
    
    // Validation errors
    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()
    
    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()
    
    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError.asStateFlow()
    
    // Form validity
    val isFormValid: StateFlow<Boolean> = combine(
        _amount,
        _description,
        _selectedCategory
    ) { amount, description, category ->
        amount.isNotEmpty() && 
        amount.toDoubleOrNull() != null && 
        amount.toDouble() > 0 &&
        description.isNotEmpty() &&
        category != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    init {
        loadCategories()
        loadPaymentMethods()
        
        if (isEditing && expenseId != null) {
            loadExpense(expenseId)
        }
    }
    
    /**
     * Load existing expense for editing
     */
    private fun loadExpense(id: String) {
        launchWithLoading {
            val expense = expenseRepository.getExpenseById(id)
            
            if (expense != null) {
                _existingExpense.value = expense
                
                // Populate form
                _amount.value = expense.amount.toPlainString()
                _description.value = expense.description
                _date.value = expense.date
                _notes.value = expense.notes ?: ""
                _receiptPhotoUri.value = expense.receiptPhotoUrl
                
                // Load and set category
                val category = categoryRepository.getCategoryById(expense.categoryId)
                _selectedCategory.value = category
                
                // Load and set payment method
                val paymentMethod = expense.paymentMethodId?.let { paymentMethodRepository.getPaymentMethodById(it) }
                _selectedPaymentMethod.value = paymentMethod
            } else {
                showError("Expense not found")
            }
        }
    }
    
    /**
     * Load available categories
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = UiState.Loading
                
                categoryRepository.getCategoriesFlow()
                    .catch { error ->
                        _categories.value = UiState.Error(
                            error.message ?: "Failed to load categories",
                            error
                        )
                    }
                    .collect { categoryList ->
                        _categories.value = UiState.Success(categoryList)
                    }
            } catch (e: Exception) {
                _categories.value = UiState.Error(e.message ?: "Unknown error", e)
            }
        }
    }
    
    /**
     * Load available payment methods
     */
    private fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                _paymentMethods.value = UiState.Loading
                
                paymentMethodRepository.getPaymentMethodsFlow()
                    .catch { error ->
                        _paymentMethods.value = UiState.Error(
                            error.message ?: "Failed to load payment methods",
                            error
                        )
                    }
                    .collect { methodList ->
                        _paymentMethods.value = UiState.Success(methodList)
                        
                        // Auto-select first payment method if none selected
                        if (_selectedPaymentMethod.value == null && methodList.isNotEmpty()) {
                            _selectedPaymentMethod.value = methodList.first()
                        }
                    }
            } catch (e: Exception) {
                _paymentMethods.value = UiState.Error(e.message ?: "Unknown error", e)
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
     * Select category
     */
    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        _categoryError.value = null
    }
    
    /**
     * Select payment method
     */
    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        _selectedPaymentMethod.value = paymentMethod
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
     * Set receipt photo
     */
    fun setReceiptPhoto(uri: String?) {
        _receiptPhotoUri.value = uri
    }
    
    /**
     * Save expense
     */
    fun saveExpense() {
        // Validate all fields
        validateAmount()
        validateDescription()
        
        if (_selectedCategory.value == null) {
            _categoryError.value = "Category is required"
            showSnackbar("Please select a category")
            return
        }
        
        if (!isFormValid.value) {
            showSnackbar("Please fix errors before saving")
            return
        }
        
        launchWithLoading {
            try {
                val amountValue = BigDecimal(_amount.value)
                val category = _selectedCategory.value!!
                val paymentMethod = _selectedPaymentMethod.value
                
                if (isEditing && expenseId != null) {
                    // Update existing expense
                    val existing = _existingExpense.value
                    if (existing != null) {
                        val updated = existing.copy(
                            amount = amountValue,
                            description = _description.value.trim(),
                            categoryId = category.id,
                            paymentMethodId = paymentMethod?.id ?: existing.paymentMethodId,
                            date = _date.value,
                            notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                            receiptPhotoUrl = _receiptPhotoUri.value,
                            updatedAt = Instant.now()
                        )
                        
                        val result = expenseRepository.updateExpense(updated)
                        
                        result.onSuccess {
                            showToast("Expense updated successfully")
                            navigateBack()
                        }
                        
                        result.onFailure { error ->
                            showError("Failed to update expense: ${error.message}")
                            Timber.e(error, "Failed to update expense")
                        }
                    }
                } else {
                    // Create new expense
                    val newExpense = Expense(
                        id = "", // Will be generated
                        userId = "", // Will be set by repository
                        amount = amountValue,
                        description = _description.value.trim(),
                        categoryId = category.id,
                        paymentMethodId = paymentMethod?.id ?: "",
                        date = _date.value,
                        notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                        receiptPhotoUrl = _receiptPhotoUri.value,
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                    
                    val result = expenseRepository.createExpense(newExpense)
                    
                    result.onSuccess {
                        showToast("Expense created successfully")
                        navigateBack()
                    }
                    
                    result.onFailure { error ->
                        showError("Failed to create expense: ${error.message}")
                        Timber.e(error, "Failed to create expense")
                    }
                }
            } catch (e: Exception) {
                showError("Error saving expense: ${e.message}")
                Timber.e(e, "Exception saving expense")
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
