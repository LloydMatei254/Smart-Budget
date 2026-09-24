package com.example.smartbudget.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartbudget.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * WorkManager worker for background data synchronization
 * Handles periodic and one-time sync operations
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val WORK_NAME = "SmartBudgetSyncWork"
        const val SYNC_TYPE_KEY = "sync_type"
        const val SYNC_TYPE_FULL = "full"
        const val SYNC_TYPE_INCREMENTAL = "incremental"
        const val SYNC_TYPE_PUSH = "push"
    }
    
    override suspend fun doWork(): Result {
        Timber.d("SyncWorker started, attempt: $runAttemptCount")
        
        return try {
            // Check if sync is needed
            if (!syncRepository.isSyncNeeded() && inputData.getString(SYNC_TYPE_KEY) != SYNC_TYPE_FULL) {
                Timber.d("No sync needed, skipping")
                return Result.success()
            }
            
            // Determine sync type
            val syncType = inputData.getString(SYNC_TYPE_KEY) ?: SYNC_TYPE_INCREMENTAL
            
            when (syncType) {
                SYNC_TYPE_FULL -> {
                    Timber.i("Performing full sync")
                    performFullSync()
                }
                SYNC_TYPE_INCREMENTAL -> {
                    Timber.i("Performing incremental sync")
                    performIncrementalSync()
                }
                SYNC_TYPE_PUSH -> {
                    Timber.i("Pushing pending changes")
                    performPushSync()
                }
                else -> {
                    Timber.w("Unknown sync type: $syncType")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Sync worker failed")
            
            // Retry on failure (WorkManager will handle backoff)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun performFullSync(): Result {
        val result = syncRepository.performFullSync()
        
        return if (result.isSuccess) {
            Timber.i("Full sync completed successfully")
            Result.success()
        } else {
            Timber.e("Full sync failed: ${result.exceptionOrNull()?.message}")
            Result.retry()
        }
    }
    
    private suspend fun performIncrementalSync(): Result {
        // First push any pending changes
        val pushResult = syncRepository.pushPendingChanges()
        if (pushResult.isFailure) {
            Timber.w("Push failed, will retry: ${pushResult.exceptionOrNull()?.message}")
        }
        
        // Then pull changes from server
        val pullResult = syncRepository.performIncrementalSync()
        
        return if (pullResult.isSuccess) {
            Timber.i("Incremental sync completed successfully")
            Result.success()
        } else {
            Timber.e("Incremental sync failed: ${pullResult.exceptionOrNull()?.message}")
            Result.retry()
        }
    }
    
    private suspend fun performPushSync(): Result {
        val result = syncRepository.pushPendingChanges()
        
        return if (result.isSuccess) {
            Timber.i("Push sync completed successfully")
            Result.success()
        } else {
            Timber.e("Push sync failed: ${result.exceptionOrNull()?.message}")
            Result.retry()
        }
    }
}
