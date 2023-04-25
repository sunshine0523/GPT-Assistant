package io.sunshine0523.gpt_assistant

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.sunshine0523.gpt_assistant.skill.Skill


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        Logger.addLogAdapter(AndroidLogAdapter())
    }

    override fun onTerminate() {
        super.onTerminate()
        Skill.freeInstance()
    }

    companion object {
        const val APP_SETTING = "app_setting"
    }
}