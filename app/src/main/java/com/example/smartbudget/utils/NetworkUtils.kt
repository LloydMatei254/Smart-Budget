package com.example.smartbudget.utils

import com.example.smartbudget.data.remote.dto.ApiResponse
import retrofit2.Response
import timber.log.Timber

/**
 * Utility functions for handling network responses
 */
object NetworkUtils {
    
    /**
     * Safe API call handler
     * Converts Retrofit Response to Result
     */
    suspend fun <T : Any> safeApiCall(
        apiCall: suspend () -> Response<ApiResponse<T>>
    ): Result<T> {
        return try {
            val response = apiCall()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val data = body.data
                    if (data != null) {
                        Result.success(data)
                    } else {
                        val errorMessage = "Response data is null"
                        Timber.e("API Error: $errorMessage")
                        Result.failure(ApiException(errorMessage, null))
                    }
                } else {
                    val errorMessage = "API call failed"
                    val errorDetail = body?.error?.toString() ?: "Unknown error"
                    Timber.e("API Error: $errorMessage - $errorDetail")
                    Result.failure(ApiException(errorMessage, errorDetail))
                }
            } else {
                val errorMessage = response.message() ?: "HTTP ${response.code()}"
                Timber.e("HTTP Error: $errorMessage")
                Result.failure(NetworkException(errorMessage, response.code()))
            }
        } catch (e: Exception) {
            Timber.e(e, "Network call failed")
            Result.failure(e)
        }
    }
    
    /**
     * Check if device has network connectivity
     */
    fun isNetworkAvailable(): Boolean {
        // This would typically check ConnectivityManager
        // For now, return true (implementation would be in a provider class)
        return true
    }
}

/**
 * Custom exception for API errors
 */
class ApiException(
    message: String,
    val error: String? = null
) : Exception(message)

/**
 * Custom exception for network/HTTP errors
 */
class NetworkException(
    message: String,
    val code: Int
) : Exception(message)
