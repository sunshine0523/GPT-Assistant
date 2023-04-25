package io.sunshine0523.gpt_assistant.ui.setup

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import io.sunshine0523.gpt_assistant.Api
import io.sunshine0523.gpt_assistant.MyApplication

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val appSetting = application.getSharedPreferences(MyApplication.APP_SETTING, Context.MODE_PRIVATE)

    fun getOpenAIKey(): String {
        return appSetting.getString(Api.OPEN_AI_KEY, "")?:""
    }

    fun setOpenAIKey(key: String) {
        appSetting.edit().apply {
            putString(Api.OPEN_AI_KEY, key)
            apply()
        }
    }

    fun getOpenAIModel(): String {
        return appSetting.getString(Api.OPEN_AI_MODEL, "")?:""
    }

    fun setOpenAIModel(model: String) {
        appSetting.edit().apply {
            putString(Api.OPEN_AI_MODEL, model)
            apply()
        }
    }

    fun getAzureKey(): String {
        return appSetting.getString(Api.AZURE_KEY, "")?:""
    }

    fun setAzureKey(key: String) {
        appSetting.edit().apply {
            putString(Api.AZURE_KEY, key)
            apply()
        }
    }

    fun getAzureEndpoint(): String {
        return appSetting.getString(Api.AZURE_ENDPOINT, "")?:""
    }

    fun setAzureEndpoint(endpoint: String) {
        appSetting.edit().apply {
            putString(Api.AZURE_ENDPOINT, endpoint)
            apply()
        }
    }

    fun getAzureDeployment(): String {
        return appSetting.getString(Api.AZURE_DEPLOYMENT, "")?:""
    }

    fun setAzureDeployment(deployment: String) {
        appSetting.edit().apply {
            putString(Api.AZURE_DEPLOYMENT, deployment)
            apply()
        }
    }

    fun getAzureSpeechKey(): String {
        return appSetting.getString(Api.AZURE_SPEECH_KEY, "")?:""
    }

    fun setAzureSpeechKey(azureSpeechKey: String) {
        appSetting.edit().apply {
            putString(Api.AZURE_SPEECH_KEY, azureSpeechKey)
            apply()
        }
    }

    fun getAzureSpeechRegion(): String {
        return appSetting.getString(Api.AZURE_SPEECH_REGION, "")?:""
    }

    fun setAzureSpeechRegion(azureSpeechRegion: String) {
        appSetting.edit().apply {
            putString(Api.AZURE_SPEECH_REGION, azureSpeechRegion)
            apply()
        }
    }

    fun getAzureSpeechLanguage(): String {
        return appSetting.getString(Api.AZURE_SPEECH_LANGUAGE, "zh-CN")?:"zh-CN"
    }

    fun setAzureSpeechLanguage(azureSpeechLanguage: String) {
        appSetting.edit().apply {
            putString(Api.AZURE_SPEECH_LANGUAGE, azureSpeechLanguage)
            apply()
        }
    }
}