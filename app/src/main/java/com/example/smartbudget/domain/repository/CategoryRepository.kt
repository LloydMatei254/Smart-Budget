package com.example.smartbudget.domain.repository

import com.example.smartbudget.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Category operations
 */
interface CategoryRepository {
    
    /**
     * Get all categories as Flow
     */
    fun getCategoriesFlow(): Flow<List<Category>>
    
    /**
     * Get all categories
     */
    suspend fun getCategories(): List<Category>
    
    /**
     * Get category by ID
     */
    suspend fun getCategoryById(categoryId: String): Category?
    
    /**
     * Get default categories
     */
    suspend fun getDefaultCategories(): List<Category>
    
    /**
     * Create new category (offline-first)
     */
    suspend fun createCategory(category: Category): Result<Category>
    
    /**
     * Update category (offline-first)
     */
    suspend fun updateCategory(category: Category): Result<Category>
    
    /**
     * Delete category (offline-first soft delete)
     */
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    
    /**
     * Sync categories with server
     */
    suspend fun syncCategories(): Result<Unit>
    
    /**
     * Force refresh from server
     */
    suspend fun refreshCategories(): Result<Unit>
}
