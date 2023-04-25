package io.sunshine0523.gpt_assistant.service

import android.R.attr.text
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.orhanobut.logger.Logger
import io.sunshine0523.gpt_assistant.MyApplication


class MyAccessibilityService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        accessibilityService = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.eventType) {
            curPackageName = event.packageName.toString()
        }
    }

    override fun onInterrupt() {

    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityService = null
    }

    companion object {
        private var curPackageName = ""

        // This may lead to memory leak, careful!
        @SuppressLint("StaticFieldLeak")
        private var accessibilityService: AccessibilityService? = null

        //==========================================================================
        //public func
        //==========================================================================

        fun getCurPackageName(): String {
            return curPackageName
        }

        fun click(resourceId: String, text: String, contentDescription: String, className: String): Boolean {
            val node = findNodeInfo(resourceId, text, contentDescription, className)
            return performClick(node)
        }

        fun paste(resourceId: String, text: String, contentDescription: String, className: String, pasteText: String): Boolean {
            val node = findNodeInfo(resourceId, text, contentDescription, className)
            return performPaste(node, pasteText)
        }

        fun key(key: String): Boolean {
            if (accessibilityService == null) return false
            when (key) {
                "back" -> {
                    accessibilityService!!.performGlobalAction(GLOBAL_ACTION_BACK)
                    return true
                }
                "home" -> {
                    accessibilityService!!.performGlobalAction(GLOBAL_ACTION_HOME)
                    return true
                }
            }
            return false
        }

        //==========================================================================
        //private func
        //==========================================================================

        private fun performClick(node: AccessibilityNodeInfo?): Boolean {
            if (node != null) {
                if (node.isClickable) {
                    val rect = Rect()
                    node.getBoundsInScreen(rect)
                    val builder = GestureDescription.Builder()
                    val p = Path()
                    p.moveTo(getCenter(rect.left.toFloat(), rect.right.toFloat()), getCenter(rect.top.toFloat(), rect.bottom.toFloat()))
                    builder.addStroke(StrokeDescription(p, 0L, 300L))
                    val gesture = builder.build()
                    accessibilityService?.dispatchGesture(gesture, null, null)

                    return true
                }
                else {
                    val parent = node.parent
                    if (parent != null) {
                        val success = performClick(parent)
                        parent.recycle()
                        return success
                    }
                }
            }
            return false
        }

        private fun performPaste(node: AccessibilityNodeInfo?, pasteText: String): Boolean {
            if (node != null) {
                val arguments = Bundle()
                arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    pasteText
                )
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                return true
            }
            return false
        }

        private fun findNodeInfo(
            id: String,
            text: String = "",
            contentDescription: String = "",
            className: String = ""
        ): AccessibilityNodeInfo? {
            if (accessibilityService == null) return null
            val nodeInfo = accessibilityService!!.rootInActiveWindow
            if (nodeInfo != null) {
                val list = nodeInfo.findAccessibilityNodeInfosByViewId(id)
                Logger.e(list.toString())
                if (list.size == 1) return list[0]
                for (n in list) {
                    val nodeInfoText =
                        if (TextUtils.isEmpty(n.text)) "" else n.text
                            .toString()
                    val nodeContentDescription =
                        if (TextUtils.isEmpty(n.contentDescription)) "" else n.contentDescription
                            .toString()
                    val nodeClassName = n.className
                    if (nodeInfoText == text && nodeContentDescription == contentDescription) {
                        if (className.isNotBlank()) {
                            if (nodeClassName == className) return n
                        } else {
                            return n
                        }
                    }
                }
            }
            return null
        }

        private fun getCenter(a: Float, b: Float): Float {
            return a + (b - a) / 2.0f
        }
    }
}