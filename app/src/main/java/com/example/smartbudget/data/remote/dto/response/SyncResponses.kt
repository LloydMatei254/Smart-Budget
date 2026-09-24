package com.example.smartbudget.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for sync operations
 */
data class SyncResponse(
    @SerializedName("syncTime")
    val syncTime: String, // ISO 8601 timestamp
    
    @SerializedName("expenses")
    val expenses: SyncData<ExpenseDto>? = null,
    
    @SerializedName("income")
    val income: SyncData<IncomeDto>? = null,
    
    @SerializedName("categories")
    val categories: SyncData<CategoryDto>? = null,
    
    @SerializedName("paymentMethods")
    val paymentMethods: SyncData<PaymentMethodDto>? = null,
    
    @SerializedName("conflicts")
    val conflicts: List<SyncConflict>? = null,
    
    @SerializedName("errors")
    val errors: List<SyncError>? = null
)

/**
 * Generic sync data container
 */
data class SyncData<T>(
    @SerializedName("created")
    val created: List<T> = emptyList(),
    
    @SerializedName("updated")
    val updated: List<T> = emptyList(),
    
    @SerializedName("deleted")
    val deleted: List<String> = emptyList(), // List of IDs
    
    @SerializedName("hasMore")
    val hasMore: Boolean = false
)

/**
 * Sync conflict information
 */
data class SyncConflict(
    @SerializedName("entityType")
    val entityType: String, // expense, income, category, payment_method
    
    @SerializedName("localId")
    val localId: String,
    
    @SerializedName("serverId")
    val serverId: String,
    
    @SerializedName("localVersion")
    val localVersion: Int,
    
    @SerializedName("serverVersion")
    val serverVersion: Int,
    
    @SerializedName("localData")
    val localData: Map<String, Any>,
    
    @SerializedName("serverData")
    val serverData: Map<String, Any>,
    
    @SerializedName("conflictType")
    val conflictType: String, // version_mismatch, deleted_on_server, etc.
    
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Sync error information
 */
data class SyncError(
    @SerializedName("entityType")
    val entityType: String,
    
    @SerializedName("localId")
    val localId: String?,
    
    @SerializedName("serverId")
    val serverId: String?,
    
    @SerializedName("operation")
    val operation: String,
    
    @SerializedName("errorCode")
    val errorCode: String,
    
    @SerializedName("errorMessage")
    val errorMessage: String,
    
    @SerializedName("timestamp")
    val timestamp: String
)
