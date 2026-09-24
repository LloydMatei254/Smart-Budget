package com.example.smartbudget.data.remote.interceptors

import com.example.smartbudget.utils.SecureStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor that adds authentication token to requests
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val secureStorage: SecureStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for login/register endpoints
        val url = originalRequest.url.toString()
        if (url.contains("/auth/login") || 
            url.contains("/auth/register") ||
            url.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        // Get access token from secure storage
        val accessToken = runBlocking {
            secureStorage.getAccessToken()
        }

        // Add Authorization header if token exists
        val newRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            Timber.w("No access token found for authenticated request")
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
