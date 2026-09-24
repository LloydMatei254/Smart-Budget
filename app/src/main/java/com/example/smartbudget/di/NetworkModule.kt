package com.example.smartbudget.di

import com.example.smartbudget.BuildConfig
import com.example.smartbudget.data.remote.api.AuthApi
import com.example.smartbudget.data.remote.api.CategoriesApi
import com.example.smartbudget.data.remote.api.ExpensesApi
import com.example.smartbudget.data.remote.api.IncomeApi
import com.example.smartbudget.data.remote.api.PaymentMethodsApi
import com.example.smartbudget.data.remote.api.ReportsApi
import com.example.smartbudget.data.remote.api.SyncApi
import com.example.smartbudget.data.remote.interceptors.AuthInterceptor
import com.example.smartbudget.data.remote.interceptors.ErrorInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provide Gson instance for JSON serialization/deserialization
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Provide HttpLoggingInterceptor for debugging network calls
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Provide OkHttpClient with interceptors
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provide Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provide AuthApi service
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    /**
     * Provide ExpensesApi service
     */
    @Provides
    @Singleton
    fun provideExpensesApi(retrofit: Retrofit): ExpensesApi {
        return retrofit.create(ExpensesApi::class.java)
    }

    /**
     * Provide IncomeApi service
     */
    @Provides
    @Singleton
    fun provideIncomeApi(retrofit: Retrofit): IncomeApi {
        return retrofit.create(IncomeApi::class.java)
    }

    /**
     * Provide CategoriesApi service
     */
    @Provides
    @Singleton
    fun provideCategoriesApi(retrofit: Retrofit): CategoriesApi {
        return retrofit.create(CategoriesApi::class.java)
    }

    /**
     * Provide PaymentMethodsApi service
     */
    @Provides
    @Singleton
    fun providePaymentMethodsApi(retrofit: Retrofit): PaymentMethodsApi {
        return retrofit.create(PaymentMethodsApi::class.java)
    }

    /**
     * Provide ReportsApi service
     */
    @Provides
    @Singleton
    fun provideReportsApi(retrofit: Retrofit): ReportsApi {
        return retrofit.create(ReportsApi::class.java)
    }

    /**
     * Provide SyncApi service
     */
    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): SyncApi {
        return retrofit.create(SyncApi::class.java)
    }
}
