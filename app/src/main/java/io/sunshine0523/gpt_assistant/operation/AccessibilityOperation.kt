package io.sunshine0523.gpt_assistant.operation

import io.sunshine0523.gpt_assistant.service.MyAccessibilityService

class AccessibilityOperation : Operation {
    fun waitAppToForeground(callback: OperationCallback, packageName: String) {
        Thread{
            //wait 10s
            var count = 100
            while (count > 0) {
                if (MyAccessibilityService.getCurPackageName() == packageName) break
                Thread.sleep(100)
                --count
            }
            callback.onFinish(count >= 0)
        }.start()
    }

    fun click(callback: OperationCallback, resourceId: String, text: String, contentDescription: String, className: String) {
        callback.onFinish(MyAccessibilityService.click(resourceId, text, contentDescription, className))
    }

    fun paste(callback: OperationCallback, resourceId: String, text: String, contentDescription: String, className: String, pasteText: String) {
        callback.onFinish(MyAccessibilityService.paste(resourceId, text, contentDescription, className, pasteText))
    }

    fun key(callback: OperationCallback, key: String) {
        callback.onFinish(MyAccessibilityService.key(key))
    }
}