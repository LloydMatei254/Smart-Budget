package com.example.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartbudget.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Category table
 */
@Dao
interface CategoryDao {
    
    /**
     * Insert or replace category
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    /**
     * Insert or replace multiple categories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    /**
     * Update category
     */
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    /**
     * Get all categories for user
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND deletedAt IS NULL ORDER BY name ASC")
    suspend fun getCategoriesForUser(userId: String): List<CategoryEntity>
    
    /**
     * Get all categories for user as Flow
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND deletedAt IS NULL ORDER BY name ASC")
    fun getCategoriesForUserFlow(userId: String): Flow<List<CategoryEntity>>
    
    /**
     * Get category by ID
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId AND deletedAt IS NULL LIMIT 1")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
    
    /**
     * Get category by ID as Flow
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId AND deletedAt IS NULL LIMIT 1")
    fun getCategoryByIdFlow(categoryId: String): Flow<CategoryEntity?>
    
    /**
     * Get default categories for user
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND isDefault = 1 AND deletedAt IS NULL")
    suspend fun getDefaultCategories(userId: String): List<CategoryEntity>
    
    /**
     * Soft delete category by ID
     */
    @Query("UPDATE categories SET deletedAt = :timestamp WHERE id = :categoryId")
    suspend fun softDeleteCategory(categoryId: String, timestamp: Long)
    
    /**
     * Hard delete category by ID
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String)
    
    /**
     * Delete all categories for user
     */
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategoriesForUser(userId: String)
    
    /**
     * Delete all categories
     */
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    /**
     * Get pending sync categories for user
     */
    @Query("SELECT * FROM categories WHERE userId = :userId AND syncStatus = 'PENDING'")
    suspend fun getPendingSyncCategories(userId: String): List<CategoryEntity>
}
