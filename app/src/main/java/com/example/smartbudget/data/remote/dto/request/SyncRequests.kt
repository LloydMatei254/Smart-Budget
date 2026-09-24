package com.example.smartbudget.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Request model for syncing local changes to server
 */
data class SyncRequest(
    @SerializedName("lastSyncTime")
    val lastSyncTime: String?, // ISO 8601 timestamp
    
    @SerializedName("expenses")
    val expenses: List<ExpenseChange>? = null,
    
    @SerializedName("income")
    val income: List<IncomeChange>? = null,
    
    @SerializedName("categories")
    val categories: List<CategoryChange>? = null,
    
    @SerializedName("paymentMethods")
    val paymentMethods: List<PaymentMethodChange>? = null,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("appVersion")
    val appVersion: String
)

/**
 * Expense change for sync
 */
data class ExpenseChange(
    @SerializedName("localId")
    val localId: String,
    
    @SerializedName("serverId")
    val serverId: String?,
    
    @SerializedName("operation")
    val operation: String, // create, update, delete
    
    @SerializedName("data")
    val data: CreateExpenseRequest?,
    
    @SerializedName("version")
    val version: Int,
    
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Income change for sync
 */
data class IncomeChange(
    @SerializedName("localId")
    val localId: String,
    
    @SerializedName("serverId")
    val serverId: String?,
    
    @SerializedName("operation")
    val operation: String, // create, update, delete
    
    @SerializedName("data")
    val data: CreateIncomeRequest?,
    
    @SerializedName("version")
    val version: Int,
    
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Category change for sync
 */
data class CategoryChange(
    @SerializedName("localId")
    val localId: String,
    
    @SerializedName("serverId")
    val serverId: String?,
    
    @SerializedName("operation")
    val operation: String, // create, update, delete
    
    @SerializedName("data")
    val data: CreateCategoryRequest?,
    
    @SerializedName("version")
    val version: Int,
    
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Payment method change for sync
 */
data class PaymentMethodChange(
    @SerializedName("localId")
    val localId: String,
    
    @SerializedName("serverId")
    val serverId: String?,
    
    @SerializedName("operation")
    val operation: String, // create, update, delete
    
    @SerializedName("data")
    val data: CreatePaymentMethodRequest?,
    
    @SerializedName("version")
    val version: Int,
    
    @SerializedName("timestamp")
    val timestamp: String
)
