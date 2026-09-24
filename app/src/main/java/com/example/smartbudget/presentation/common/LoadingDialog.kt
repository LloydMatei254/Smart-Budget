package com.example.smartbudget.presentation.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import com.example.smartbudget.R

/**
 * Custom loading dialog to show progress with optional message
 */
class LoadingDialog(context: Context) {
    
    private val dialog: Dialog = Dialog(context)
    private var messageTextView: TextView? = null
    
    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // We'll create a simple layout programmatically since we don't have the XML yet
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    
    /**
     * Show loading dialog with optional message
     */
    fun show(message: String = "Loading...") {
        if (!dialog.isShowing) {
            messageTextView?.text = message
            dialog.show()
        }
    }
    
    /**
     * Update loading message
     */
    fun updateMessage(message: String) {
        messageTextView?.text = message
    }
    
    /**
     * Dismiss loading dialog
     */
    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    
    /**
     * Check if dialog is showing
     */
    fun isShowing(): Boolean = dialog.isShowing
}
