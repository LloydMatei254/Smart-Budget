package com.example.smartbudget.data.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for scheduling and controlling background synchronization
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val PERIODIC_SYNC_WORK_NAME = "PeriodicSyncWork"
        private const val ONE_TIME_SYNC_WORK_NAME = "OneTimeSyncWork"
        private const val SYNC_INTERVAL_HOURS = 6L // Sync every 6 hours
        private const val SYNC_FLEX_INTERVAL_HOURS = 1L // Allow 1 hour flex for battery optimization
    }
    
    /**
     * Schedule periodic background sync
     * Runs every 6 hours when device conditions are met
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = SYNC_INTERVAL_HOURS,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = SYNC_FLEX_INTERVAL_HOURS,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    SyncWorker.SYNC_TYPE_KEY to SyncWorker.SYNC_TYPE_INCREMENTAL
                )
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(PERIODIC_SYNC_WORK_NAME)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        
        Timber.i("Periodic sync scheduled (every $SYNC_INTERVAL_HOURS hours)")
    }
    
    /**
     * Cancel periodic sync
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        Timber.i("Periodic sync cancelled")
    }
    
    /**
     * Trigger immediate one-time sync
     */
    fun triggerImmediateSync(syncType: String = SyncWorker.SYNC_TYPE_INCREMENTAL) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    SyncWorker.SYNC_TYPE_KEY to syncType
                )
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        
        workManager.enqueueUniqueWork(
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
        
        Timber.i("Immediate sync triggered (type: $syncType)")
    }
    
    /**
     * Trigger full sync (pull all data from server)
     */
    fun triggerFullSync() {
        triggerImmediateSync(SyncWorker.SYNC_TYPE_FULL)
    }
    
    /**
     * Push pending changes immediately
     */
    fun pushPendingChanges() {
        triggerImmediateSync(SyncWorker.SYNC_TYPE_PUSH)
    }
    
    /**
     * Check if sync is currently running
     */
    fun isSyncRunning(): Boolean {
        val workInfos = workManager.getWorkInfosByTag(PERIODIC_SYNC_WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.RUNNING }
    }
    
    /**
     * Get sync status as LiveData
     */
    fun getSyncStatus() = workManager.getWorkInfosForUniqueWorkLiveData(PERIODIC_SYNC_WORK_NAME)
    
    /**
     * Cancel all sync work
     */
    fun cancelAllSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        workManager.cancelUniqueWork(ONE_TIME_SYNC_WORK_NAME)
        Timber.i("All sync work cancelled")
    }
}
