package com.example.smartbudget.data.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor that handles common HTTP errors
 */
@Singleton
class ErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            // Log errors
            if (!response.isSuccessful) {
                Timber.w("HTTP ${response.code} for ${request.url}")
                
                when (response.code) {
                    401 -> {
                        Timber.e("Unauthorized request - token may be expired")
                        // TODO: Trigger token refresh or logout
                    }
                    403 -> {
                        Timber.e("Forbidden - insufficient permissions")
                    }
                    404 -> {
                        Timber.e("Resource not found: ${request.url}")
                    }
                    429 -> {
                        Timber.e("Rate limit exceeded")
                    }
                    in 500..599 -> {
                        Timber.e("Server error: ${response.code}")
                    }
                }
            }

            return response
        } catch (e: IOException) {
            Timber.e(e, "Network error for ${request.url}")
            throw e
        }
    }
}
