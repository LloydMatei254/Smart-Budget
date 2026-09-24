package com.example.smartbudget.presentation.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Extension functions for Flow collection in lifecycle-aware manner
 */

/**
 * Collect Flow in a lifecycle-aware manner
 * Automatically starts and stops collection based on lifecycle state
 */
fun <T> Flow<T>.collectInLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collect(collector)
        }
    }
}

/**
 * Collect latest Flow value in a lifecycle-aware manner
 */
fun <T> Flow<T>.collectLatestInLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collectLatest(collector)
        }
    }
}
