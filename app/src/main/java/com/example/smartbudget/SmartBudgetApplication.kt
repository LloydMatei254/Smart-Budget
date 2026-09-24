package com.example.smartbudget

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.smartbudget.data.worker.SyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for Smart Budget.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Implements Configuration.Provider for WorkManager with Hilt.
 */
@HiltAndroidApp
class SmartBudgetApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Plant a release tree that only logs warnings and errors
            Timber.plant(ReleaseTree())
        }
        
        Timber.d("SmartBudgetApplication initialized")
        
        // Schedule periodic background sync
        syncManager.schedulePeriodicSync()
        Timber.d("Background sync scheduled")
    }
    
    /**
     * Provide WorkManager configuration with Hilt worker factory
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    /**
     * Release tree that only logs warnings and errors in production
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                // In production, you could send these to a crash reporting service
                // like Firebase Crashlytics or Sentry
            }
        }
    }
}
