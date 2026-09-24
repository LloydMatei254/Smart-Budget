package com.example.smartbudget.domain.repository

import java.time.Instant

/**
 * Repository interface for synchronization operations
 */
interface SyncRepository {
    
    /**
     * Perform full synchronization (pull all changes from server)
     */
    suspend fun performFullSync(): Result<Unit>
    
    /**
     * Perform incremental sync (only changes since last sync)
     */
    suspend fun performIncrementalSync(): Result<Unit>
    
    /**
     * Push local pending changes to server
     */
    suspend fun pushPendingChanges(): Result<Unit>
    
    /**
     * Get last sync timestamp
     */
    suspend fun getLastSyncTime(): Instant?
    
    /**
     * Update last sync timestamp
     */
    suspend fun updateLastSyncTime(timestamp: Instant)
    
    /**
     * Get count of pending changes
     */
    suspend fun getPendingChangesCount(): Int
    
    /**
     * Check if sync is needed
     */
    suspend fun isSyncNeeded(): Boolean
}
