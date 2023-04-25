package io.sunshine0523.gpt_assistant.operation

/**
 * 当任务完成后的回调函数
 */
interface OperationCallback {
    fun onFinish(execResult: Boolean, operationResult: LinkedHashMap<String, String> = LinkedHashMap())
}