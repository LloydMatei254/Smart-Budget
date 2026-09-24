package com.example.smartbudget.presentation.categories

import androidx.lifecycle.viewModelScope
import com.example.smartbudget.domain.model.Category
import com.example.smartbudget.domain.repository.CategoryRepository
import com.example.smartbudget.presentation.common.BaseViewModel
import com.example.smartbudget.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

/**
 * ViewModel for Categories screen
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {
    
    // Categories state
    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()
    
    // Selected category for editing
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    
    // Dialog state
    private val _showCategoryDialog = MutableStateFlow(false)
    val showCategoryDialog: StateFlow<Boolean> = _showCategoryDialog.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Filtered categories based on search
    val filteredCategories: StateFlow<List<Category>> = combine(
        _categories,
        _searchQuery
    ) { categoriesState, query ->
        if (categoriesState is UiState.Success) {
            if (query.isEmpty()) {
                categoriesState.data
            } else {
                categoriesState.data.filter { category ->
                    category.name.contains(query, ignoreCase = true)
                }
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        loadCategories()
    }
    
    /**
     * Load all categories
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = UiState.Loading
                
                // Observe categories flow from repository
                categoryRepository.getCategoriesFlow()
                    .catch { error ->
                        _categories.value = UiState.Error(
                            error.message ?: "Failed to load categories",
                            error
                        )
                        Timber.e(error, "Failed to load categories")
                    }
                    .collect { categoryList ->
                        _categories.value = UiState.Success(categoryList)
                        Timber.d("Loaded ${categoryList.size} categories")
                    }
            } catch (e: Exception) {
                _categories.value = UiState.Error(
                    e.message ?: "Unknown error",
                    e
                )
                Timber.e(e, "Exception loading categories")
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
     * Show add category dialog
     */
    fun showAddCategoryDialog() {
        _selectedCategory.value = null
        _showCategoryDialog.value = true
    }
    
    /**
     * Show edit category dialog
     */
    fun showEditCategoryDialog(category: Category) {
        _selectedCategory.value = category
        _showCategoryDialog.value = true
    }
    
    /**
     * Hide category dialog
     */
    fun hideCategoryDialog() {
        _showCategoryDialog.value = false
        _selectedCategory.value = null
    }
    
    /**
     * Create new category
     */
    fun createCategory(name: String, color: String, icon: String) {
        if (name.isEmpty()) {
            showSnackbar("Category name is required")
            return
        }
        
        launchWithLoading {
            val category = Category(
                id = "", // Will be generated
                userId = "", // Will be set by repository
                name = name.trim(),
                color = color,
                icon = icon,
                isDefault = false,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            val result = categoryRepository.createCategory(category)
            
            result.onSuccess {
                showToast("Category created successfully")
                hideCategoryDialog()
            }
            
            result.onFailure { error ->
                showError("Failed to create category: ${error.message}")
                Timber.e(error, "Failed to create category")
            }
        }
    }
    
    /**
     * Update existing category
     */
    fun updateCategory(category: Category, name: String, color: String, icon: String) {
        if (name.isEmpty()) {
            showSnackbar("Category name is required")
            return
        }
        
        launchWithLoading {
            val updatedCategory = category.copy(
                name = name.trim(),
                color = color,
                icon = icon,
                updatedAt = Instant.now()
            )
            
            val result = categoryRepository.updateCategory(updatedCategory)
            
            result.onSuccess {
                showToast("Category updated successfully")
                hideCategoryDialog()
            }
            
            result.onFailure { error ->
                showError("Failed to update category: ${error.message}")
                Timber.e(error, "Failed to update category")
            }
        }
    }
    
    /**
     * Delete category
     */
    fun deleteCategory(category: Category) {
        sendEvent(
            com.example.smartbudget.presentation.common.UiEvent.ShowConfirmation(
                title = "Delete Category",
                message = "Are you sure you want to delete '${category.name}'? This action cannot be undone.",
                confirmLabel = "Delete",
                onConfirm = {
                    performDeleteCategory(category)
                }
            )
        )
    }
    
    private fun performDeleteCategory(category: Category) {
        launchWithLoading {
            val result = categoryRepository.deleteCategory(category.id)
            
            result.onSuccess {
                showToast("Category deleted successfully")
            }
            
            result.onFailure { error ->
                showError("Failed to delete category: ${error.message}")
                Timber.e(error, "Failed to delete category")
            }
        }
    }
    
    /**
     * Refresh categories from server
     */
    fun refresh() {
        launchSilent {
            categoryRepository.refreshCategories()
                .onSuccess {
                    showToast("Categories synced")
                }
                .onFailure { error ->
                    showSnackbar("Sync failed: ${error.message}")
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

/**
 * Data class for category form state
 */
data class CategoryFormState(
    val name: String = "",
    val color: String = "#3498DB",
    val icon: String = "shopping_cart",
    val isEditing: Boolean = false
)
