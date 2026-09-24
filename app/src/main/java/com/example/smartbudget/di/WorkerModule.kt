package com.example.smartbudget.di

import android.content.Context
import androidx.work.WorkManager
import com.example.smartbudget.data.worker.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing WorkManager dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    /**
     * Provide WorkManager instance
     */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    /**
     * Provide SyncManager instance
     */
    @Provides
    @Singleton
    fun provideSyncManager(@ApplicationContext context: Context): SyncManager {
        return SyncManager(context)
    }
}
