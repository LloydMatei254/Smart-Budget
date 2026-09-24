package com.example.smartbudget.presentation.common

import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged

/**
 * Extension functions for View operations
 */

/**
 * Show view with animation
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
 * Hide view with animation
 */
fun View.hide() {
    visibility = View.GONE
}

/**
 * Make view invisible (still takes up space)
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Toggle view visibility
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * Set view visibility based on condition
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Enable/disable view with alpha animation
 */
fun View.setEnabled(enabled: Boolean, disabledAlpha: Float = 0.5f) {
    isEnabled = enabled
    alpha = if (enabled) 1f else disabledAlpha
}

/**
 * Get text from EditText as String
 */
fun EditText.textString(): String = text.toString().trim()

/**
 * Set text change listener with debounce
 */
fun EditText.onTextChanged(action: (String) -> Unit) {
    doAfterTextChanged { editable ->
        action(editable?.toString()?.trim() ?: "")
    }
}

/**
 * Clear EditText
 */
fun EditText.clear() {
    setText("")
}

/**
 * Check if EditText is empty
 */
fun EditText.isEmpty(): Boolean = textString().isEmpty()

/**
 * Check if EditText is not empty
 */
fun EditText.isNotEmpty(): Boolean = textString().isNotEmpty()
