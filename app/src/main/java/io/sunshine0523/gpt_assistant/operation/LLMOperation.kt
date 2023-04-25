package io.sunshine0523.gpt_assistant.operation

import android.content.Context
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.AzureOpenAiService
import com.theokanning.openai.service.OpenAiService
import io.sunshine0523.gpt_assistant.Api
import io.sunshine0523.gpt_assistant.R
import java.time.Duration

class LLMOperation(private val context: Context) : Operation{
    private val openAiService: OpenAiService =
        if (Api.isAzure) {
            AzureOpenAiService(Api.key, Api.endpoint, Api.model, Duration.ZERO)
        } else {
            OpenAiService(Api.key, Duration.ZERO)
        }

    fun summarizeText(callback: OperationCallback, text: String) {
        try {
            val completionResult = callOpenAI("user", "${context.getString(R.string.summarize_text_prompt)}\n$text", 0.9, 2000)
            callback.onFinish(true, LinkedHashMap<String, String>().apply { put("summarizedText", completionResult.choices[0].message.content) })
        } catch (e: Exception) {
            callback.onFinish(false)
        }
    }

    fun knowledgeQA(callback: OperationCallback, question: String) {
        try {
            val completionResult = callOpenAI("user", question, 0.9, 2000)
            callback.onFinish(true, LinkedHashMap<String, String>().apply {
                put("question", question)
                put("answer", completionResult.choices[0].message.content)
            })
        } catch (e: Exception) {
            callback.onFinish(false)
        }
    }

    private fun callOpenAI(role: String, content: String, temperature: Double, maxTokens: Int): ChatCompletionResult {
        return if (Api.isAzure) {
            openAiService as AzureOpenAiService
            openAiService.createChatCompletion(ChatCompletionRequest().apply {
                messages = ArrayList<ChatMessage?>().apply {
                    add(ChatMessage(
                        role,
                        content)
                    )
                }
                this.temperature = temperature
                this.maxTokens = maxTokens
            }, Api.AZURE_VERSION_20230315)
        } else {
            openAiService.createChatCompletion(ChatCompletionRequest().apply {
                messages = ArrayList<ChatMessage?>().apply {
                    add(ChatMessage(
                        "user",
                        content)
                    )
                }
                this.temperature = temperature
                this.maxTokens = maxTokens
            })
        }
    }
}