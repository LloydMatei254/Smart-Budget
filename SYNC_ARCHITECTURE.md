# Smart Budget - Synchronization Architecture

## Overview
Smart Budget implements a robust offline-first synchronization system using WorkManager for reliable background data syncing between the local Room database and the remote REST API.

## Architecture Components

### 1. SyncRepository
**Location**: `domain/repository/SyncRepository.kt`

Interface defining sync operations:
- `performFullSync()` - Pull all data from server
- `performIncrementalSync()` - Pull only changes since last sync
- `pushPendingChanges()` - Push local changes to server
- `getLastSyncTime()` - Get timestamp of last successful sync
- `getPendingChangesCount()` - Count of unsynced local changes
- `isSyncNeeded()` - Check if sync is required

### 2. SyncRepositoryImpl
**Location**: `data/repository/SyncRepositoryImpl.kt`

Implementation with three sync strategies:

#### Full Sync
- Pulls all data from server
- Used for initial sync or data recovery
- Updates all local entities with server data
- Updates last sync timestamp

#### Incremental Sync
1. Push pending local changes first
2. Pull changes since last sync timestamp
3. Apply creates, updates, and deletes
4. Handle conflicts (logged for manual resolution)
5. Update last sync timestamp

#### Push Sync
- Collects pending changes from all DAOs
- Converts entities to sync request format
- Pushes to server via SyncApi
- Updates local records with server IDs on success
- Marks records as "synced"

### 3. SyncWorker
**Location**: `data/worker/SyncWorker.kt`

WorkManager CoroutineWorker that:
- Runs in background with Hilt injection support
- Supports three sync types: FULL, INCREMENTAL, PUSH
- Implements automatic retry with exponential backoff
- Maximum 3 retry attempts before failure
- Logs all operations for debugging

### 4. SyncManager
**Location**: `data/worker/SyncManager.kt`

High-level API for sync management:

#### Periodic Sync
```kotlin
syncManager.schedulePeriodicSync()
```
- Runs every 6 hours with 1-hour flex window
- Requires network connection
- Requires battery not low
- Uses KEEP policy (won't reschedule if already scheduled)
- Exponential backoff on failures

#### One-Time Sync
```kotlin
syncManager.triggerImmediateSync(SyncWorker.SYNC_TYPE_INCREMENTAL)
syncManager.triggerFullSync()
syncManager.pushPendingChanges()
```
- Runs immediately when conditions met
- Uses REPLACE policy (cancels existing one-time sync)
- Expedited when possible

#### Sync Status Monitoring
```kotlin
val isSyncing = syncManager.isSyncRunning()
val statusLiveData = syncManager.getSyncStatus()
```

## Sync Flow

### User Creates/Updates/Deletes Data
1. Action saved to Room immediately → UI updates instantly
2. Record marked `SyncStatus.PENDING`
3. If network available → attempt immediate sync
4. On success → update with server ID, mark `SYNCED`
5. On failure → remains `PENDING`, will sync via WorkManager

### Background Sync (Every 6 Hours)
1. WorkManager triggers SyncWorker
2. Check if sync needed (pending changes exist)
3. Push pending changes to server
4. Pull changes since last sync
5. Resolve any conflicts
6. Update last sync timestamp

### Manual Sync (User Triggers)
```kotlin
// From ViewModel or repository
syncManager.triggerImmediateSync()
```

## Conflict Resolution

### Strategy: Last-Write-Wins with Version Control
- Each entity has `syncVersion` field
- Server increments version on each update
- If local version < server version → server wins
- If local version > server version → local wins (shouldn't happen)
- Conflicts logged and exposed to UI for manual resolution

### Conflict Detection
```kotlin
if (localVersion != serverVersion) {
    // Conflict detected
    logConflict(entityType, localId, serverId)
    // Show conflict resolution UI
}
```

## Network Constraints

### WorkManager Constraints
```kotlin
Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()
```

### Network Availability Check
```kotlin
NetworkUtils.isNetworkAvailable()
```
Used before all API calls to avoid unnecessary failures.

## Error Handling

### Retry Strategy
- **WorkManager Backoff**: Exponential with minimum backoff time
- **Max Attempts**: 3 retries before marking as failed
- **Retry Conditions**: Network errors, server 5xx errors
- **No Retry**: Client errors (4xx), authentication failures

### Error Logging
All sync operations logged with Timber:
- Debug: Operation start/completion
- Info: Sync success with counts
- Warning: Push failures, conflicts detected
- Error: Sync failures with exception details

## Integration with Repositories

All repositories implement offline-first pattern:

```kotlin
// Example: ExpenseRepositoryImpl
override suspend fun createExpense(expense: Expense): Result<Expense> {
    // 1. Save locally immediately
    expenseDao.insertExpense(expense.toEntity())
    
    // 2. Try immediate sync if online
    if (NetworkUtils.isNetworkAvailable()) {
        val result = safeApiCall { expensesApi.createExpense(...) }
        result.onSuccess { serverExpense ->
            // Update with server ID
            expenseDao.updateExpense(...)
        }
    }
    // 3. If offline or sync fails, WorkManager will handle it
    
    return Result.success(expense)
}
```

## Hilt Integration

### WorkManager Configuration
```kotlin
@HiltAndroidApp
class SmartBudgetApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

### Worker Factory
Uses `@HiltWorker` annotation for dependency injection:
```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams)
```

## Testing Sync

### Manual Trigger
```kotlin
// In debug menu or settings
syncManager.triggerFullSync()
```

### Monitor Sync Status
```kotlin
syncManager.getSyncStatus().observe(this) { workInfoList ->
    workInfoList.forEach { workInfo ->
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> showSyncPending()
            WorkInfo.State.RUNNING -> showSyncInProgress()
            WorkInfo.State.SUCCEEDED -> showSyncSuccess()
            WorkInfo.State.FAILED -> showSyncError()
            else -> {}
        }
    }
}
```

### Check Pending Changes
```kotlin
val pendingCount = syncRepository.getPendingChangesCount()
if (pendingCount > 0) {
    showPendingChangesIndicator(pendingCount)
}
```

## Performance Considerations

### Batch Operations
- Uses bulk insert operations for efficiency
- Processes all entities of same type together
- Single database transaction per entity type

### Network Efficiency
- Incremental sync only pulls changes since last sync
- Pagination support for large datasets
- Compression headers for API requests

### Battery Optimization
- Only syncs when battery not low
- Uses WorkManager's built-in battery optimization
- Flex interval allows system to batch work

## Future Enhancements

1. **Conflict Resolution UI**: Show users conflicts and let them choose
2. **Selective Sync**: Allow users to choose what to sync
3. **Sync Priority**: High-priority changes sync immediately
4. **Bandwidth Monitoring**: Respect metered connections
5. **Delta Sync**: Only send changed fields, not entire records
6. **Compression**: Gzip request/response bodies
7. **Encryption**: End-to-end encryption for sensitive data

## Monitoring & Observability

### Metrics to Track
- Sync success rate
- Average sync duration
- Pending changes count over time
- Conflict frequency
- Network failure rate

### Logging
All sync operations logged with structured data:
```
[SyncWorker] Starting incremental sync, attempt: 1
[SyncRepository] Pushing 5 pending changes
[SyncRepository] Incremental sync: 3 created, 2 updated, 0 deleted
[SyncWorker] Sync completed successfully in 1.2s
```

## Security

### Data in Transit
- HTTPS only (enforced by network security config)
- Certificate pinning (optional, for production)
- JWT authentication for all API calls

### Data at Rest
- Room database encrypted with SQLCipher (optional)
- Sensitive tokens in EncryptedSharedPreferences
- No plaintext passwords stored

## Summary

The synchronization system provides:
- ✅ Reliable offline-first operation
- ✅ Automatic background sync every 6 hours
- ✅ Manual sync on demand
- ✅ Conflict detection and logging
- ✅ Network-aware with retry logic
- ✅ Battery-efficient with constraints
- ✅ Full observability with Timber logging
- ✅ Seamless integration with repositories
- ✅ Hilt dependency injection throughout
