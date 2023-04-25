package io.sunshine0523.gpt_assistant.view

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.sunshine0523.gpt_assistant.R

class LoadingView(private val context: Context, private val message: String) {
    private val loadingDialog = MaterialAlertDialogBuilder(context).apply {
        setTitle(context.getString(R.string.loading))
        setMessage(message)
        setCancelable(false)
    }.create()

    fun show() {
        loadingDialog.show()
    }

    fun dismiss() {
        loadingDialog.dismiss()
    }
}