package com.example.smartbudget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartbudget.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User table
 */
@Dao
interface UserDao {
    
    /**
     * Insert or replace user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * Update user
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * Get user by ID
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?
    
    /**
     * Get user by ID as Flow
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>
    
    /**
     * Get current user (assumes single user per device)
     */
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    /**
     * Get current user as Flow
     */
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>
    
    /**
     * Delete user by ID
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
    
    /**
     * Delete all users
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
