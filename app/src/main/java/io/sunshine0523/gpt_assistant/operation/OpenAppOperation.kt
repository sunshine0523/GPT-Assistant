package io.sunshine0523.gpt_assistant.operation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.orhanobut.logger.Logger

class OpenAppOperation(private val context: Context) : Operation {
    fun startActivity(callback: OperationCallback, packageName: String, activityName: String) {
        try {
            context.startActivity(Intent().apply {
                component = ComponentName(packageName, activityName)
            })
        } catch (e: Exception) {
            callback.onFinish(false, LinkedHashMap<String, String>().apply { put("Error", "${e.message}") })
        }
        callback.onFinish(true)
    }

    fun startActivity(callback: OperationCallback, packageName: String) {
        try {
            context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
        } catch (e: Exception) {
            callback.onFinish(false, LinkedHashMap<String, String>().apply { put("Error", "${e.message}") })
        }
        callback.onFinish(true)
    }
}