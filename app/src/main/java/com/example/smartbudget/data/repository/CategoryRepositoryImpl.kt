package com.example.smartbudget.data.repository

import com.example.smartbudget.data.local.dao.CategoryDao
import com.example.smartbudget.data.local.entities.CategoryEntity
import com.example.smartbudget.data.mapper.CategoryMapper.toDomain
import com.example.smartbudget.data.mapper.CategoryMapper.toEntity
import com.example.smartbudget.data.remote.api.CategoriesApi
import com.example.smartbudget.data.remote.dto.request.CreateCategoryRequest
import com.example.smartbudget.data.remote.dto.request.UpdateCategoryRequest
import com.example.smartbudget.di.IoDispatcher
import com.example.smartbudget.domain.model.Category
import com.example.smartbudget.domain.model.SyncStatus
import com.example.smartbudget.domain.repository.CategoryRepository
import com.example.smartbudget.utils.NetworkUtils
import com.example.smartbudget.utils.PreferencesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoriesApi: CategoriesApi,
    private val preferencesManager: PreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {
    
    override fun getCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getCategoriesForUserFlow(getCurrentUserId())
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun getCategories(): List<Category> = withContext(ioDispatcher) {
        categoryDao.getCategoriesForUser(getCurrentUserId()).map { it.toDomain() }
    }
    
    override suspend fun getCategoryById(categoryId: String): Category? =
        withContext(ioDispatcher) {
            categoryDao.getCategoryById(categoryId)?.toDomain()
        }
    
    override suspend fun getDefaultCategories(): List<Category> =
        withContext(ioDispatcher) {
            categoryDao.getCategoriesForUser(getCurrentUserId())
                .filter { it.isDefault }
                .map { it.toDomain() }
        }
    
    override suspend fun createCategory(category: Category): Result<Category> =
        withContext(ioDispatcher) {
            try {
                val localId = UUID.randomUUID().toString()
                val now = Instant.now()
                
                // Create entity with sync fields
                val entity = CategoryEntity(
                    id = if (category.id.isEmpty()) UUID.randomUUID().toString() else category.id,
                    localId = localId,
                    userId = category.userId,
                    name = category.name,
                    color = category.color,
                    icon = category.icon,
                    isDefault = category.isDefault,
                    createdAt = now.toEpochMilli(),
                    updatedAt = now.toEpochMilli(),
                    deletedAt = null,
                    syncStatus = "PENDING"
                )
                
                categoryDao.insertCategory(entity)
                Timber.d("Category saved locally: ${category.name}")
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        val request = CreateCategoryRequest(
                            name = category.name,
                            color = category.color,
                            icon = category.icon
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            categoriesApi.createCategory(request)
                        }
                        
                        result.onSuccess { serverCategory ->
                            val syncedEntity = entity.copy(
                                id = serverCategory.id,
                                syncStatus = "SYNCED"
                            )
                            categoryDao.insertCategory(syncedEntity)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync category")
                    }
                }
                
                Result.success(entity.toDomain())
            } catch (e: Exception) {
                Timber.e(e, "Failed to create category")
                Result.failure(e)
            }
        }
    
    override suspend fun updateCategory(category: Category): Result<Category> =
        withContext(ioDispatcher) {
            try {
                // Get existing entity to preserve sync fields
                val existingEntity = categoryDao.getCategoryById(category.id)
                if (existingEntity == null) {
                    return@withContext Result.failure(Exception("Category not found"))
                }
                
                val updatedEntity = existingEntity.copy(
                    name = category.name,
                    color = category.color,
                    icon = category.icon,
                    isDefault = category.isDefault,
                    updatedAt = Instant.now().toEpochMilli(),
                    syncStatus = "PENDING"
                )
                
                categoryDao.updateCategory(updatedEntity)
                
                if (NetworkUtils.isNetworkAvailable() && category.id.isNotEmpty()) {
                    try {
                        val request = UpdateCategoryRequest(
                            name = category.name,
                            color = category.color,
                            icon = category.icon
                        )
                        
                        val result = NetworkUtils.safeApiCall {
                            categoriesApi.updateCategory(category.id, request)
                        }
                        
                        result.onSuccess {
                            val syncedEntity = updatedEntity.copy(syncStatus = "SYNCED")
                            categoryDao.updateCategory(syncedEntity)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to sync category update")
                    }
                }
                
                Result.success(updatedEntity.toDomain())
            } catch (e: Exception) {
                Timber.e(e, "Failed to update category")
                Result.failure(e)
            }
        }
    
    override suspend fun deleteCategory(categoryId: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                categoryDao.softDeleteCategory(categoryId, Instant.now().toEpochMilli())
                
                if (NetworkUtils.isNetworkAvailable()) {
                    try {
                        NetworkUtils.safeApiCall {
                            categoriesApi.deleteCategory(categoryId)
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to delete category on server")
                    }
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete category")
                Result.failure(e)
            }
        }
    
    override suspend fun syncCategories(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val pendingCategories = categoryDao.getPendingSyncCategories(getCurrentUserId())
            
            pendingCategories.forEach { entity ->
                val category = entity.toDomain()
                if (category.id.isEmpty()) {
                    createCategory(category)
                } else {
                    updateCategory(category)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            Result.failure(e)
        }
    }
    
    override suspend fun refreshCategories(): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (!NetworkUtils.isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection"))
            }
            
            val result = NetworkUtils.safeApiCall {
                categoriesApi.getCategories()
            }
            
            result.onSuccess { categories ->
                val entities = categories.map { dto -> dto.toEntity(getCurrentUserId()) }
                categoryDao.insertCategories(entities)
                Timber.d("Refreshed ${entities.size} categories")
            }
            
            result.map { Unit }
        } catch (e: Exception) {
            Timber.e(e, "Refresh failed")
            Result.failure(e)
        }
    }
    
    private fun getCurrentUserId(): String {
        return runBlocking {
            preferencesManager.getUserIdSync() ?: ""
        }
    }
}
