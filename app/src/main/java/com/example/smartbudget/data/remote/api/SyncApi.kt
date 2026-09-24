package com.example.smartbudget.data.remote.api

import com.example.smartbudget.data.remote.dto.ApiResponse
import com.example.smartbudget.data.remote.dto.request.SyncRequest
import com.example.smartbudget.data.remote.dto.response.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API interface for Sync endpoints
 */
interface SyncApi {
    
    /**
     * Get changes since last sync
     */
    @GET("sync/changes")
    suspend fun getChanges(
        @Query("lastSyncTime") lastSyncTime: String, // ISO 8601 timestamp
        @Query("entityTypes") entityTypes: List<String>? = null // expenses, income, categories, payment_methods
    ): Response<ApiResponse<SyncResponse>>
    
    /**
     * Push local changes to server
     */
    @POST("sync/push")
    suspend fun pushChanges(
        @Body request: SyncRequest
    ): Response<ApiResponse<SyncResponse>>
    
    /**
     * Full sync - get all data
     */
    @POST("sync/full")
    suspend fun fullSync(
        @Body request: SyncRequest
    ): Response<ApiResponse<SyncResponse>>
    
    /**
     * Get sync status
     */
    @GET("sync/status")
    suspend fun getSyncStatus(): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * Resolve sync conflict
     */
    @POST("sync/resolve-conflict")
    suspend fun resolveConflict(
        @Body conflictResolution: Map<String, Any>
    ): Response<ApiResponse<Unit>>
}
