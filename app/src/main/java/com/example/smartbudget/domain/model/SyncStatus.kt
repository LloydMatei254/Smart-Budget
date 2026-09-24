package com.example.smartbudget.domain.model

/**
 * Synchronization status for transactions
 */
enum class SyncStatus {
    /**
     * Transaction is synchronized with the server
     */
    SYNCED,

    /**
     * Transaction has local changes that need to be synced
     */
    PENDING,

    /**
     * Transaction has a conflict (server version differs from local)
     */
    CONFLICT,

    /**
     * Transaction is marked for deletion
     */
    DELETED;

    companion object {
        fun fromString(value: String): SyncStatus {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: SYNCED
        }
    }
}
