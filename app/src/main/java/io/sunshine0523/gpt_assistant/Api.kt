package io.sunshine0523.gpt_assistant

object Api {
    const val OPENAI_BASE_URL = "https://api.openai.com/"
    const val AZURE_VERSION_20221201 = "2022-12-01"
    const val AZURE_VERSION_20230315 = "2023-03-15-preview"

    const val OPEN_AI_KEY = "open_ai_key"
    const val OPEN_AI_MODEL = "open_ai_model"
    const val AZURE_KEY = "azure_key"
    const val AZURE_ENDPOINT = "azure_endpoint"
    const val AZURE_DEPLOYMENT = "azure_deployment"
    const val AZURE_SPEECH_KEY = "azure_speech_key"
    const val AZURE_SPEECH_REGION = "azure_speech_region"
    const val AZURE_SPEECH_LANGUAGE = "azure_speech_language"

    var isAzure = false
    var key = ""
    var model = ""
    var endpoint = OPENAI_BASE_URL
    var azureSpeechKey = ""
    var azureSpeechRegion = ""
    var azureSpeechLanguage = "zh-CN"
}