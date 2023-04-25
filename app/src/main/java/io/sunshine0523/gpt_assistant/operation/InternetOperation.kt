package io.sunshine0523.gpt_assistant.operation

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class InternetOperation : Operation {
    fun accessWeb(callback: OperationCallback, webUrl: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(if (webUrl.startsWith("http://") || webUrl.startsWith("https://")) webUrl else "https://$webUrl")
            .build()
        try {
            val response = client.newCall(request).execute()
            val htmlStr = response.body?.string()?:""
            val doc = Jsoup.parse(htmlStr)
            callback.onFinish(true, LinkedHashMap<String, String>().apply { put("webContent", doc.body().text()) })
        } catch (e: Exception) {
            callback.onFinish(false)
        }
    }
}