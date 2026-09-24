package com.example.smartbudget.presentation.common

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provider for accessing Android resources in ViewModels
 * Allows ViewModels to get strings and resources without direct Context dependency
 */
@Singleton
class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get string resource
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }
    
    /**
     * Get string resource with format args
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
    
    /**
     * Get color resource
     */
    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }
}
