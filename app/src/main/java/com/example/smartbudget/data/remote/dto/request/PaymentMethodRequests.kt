package com.example.smartbudget.data.remote.dto.request

import com.google.gson.annotations.SerializedName

/**
 * Create payment method request DTO
 */
data class CreatePaymentMethodRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

/**
 * Update payment method request DTO
 */
data class UpdatePaymentMethodRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("isDefault")
    val isDefault: Boolean
)
