package io.sunshine0523.gpt_assistant.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.orhanobut.logger.Logger
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.skill.ExecSkillCallback
import io.sunshine0523.gpt_assistant.skill.Skill
import io.sunshine0523.gpt_assistant.skill.SkillBean
import io.sunshine0523.gpt_assistant.ui.floating.FloatingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AssistantService : Service() {

    private val scope = MainScope()

    override fun onBind(intent: Intent): IBinder {
        return AssistantServiceBinder()
    }

    fun execSkill(skillBeanList: ArrayList<SkillBean>?, callback: ExecSkillCallback) {
        val skill = Skill.getInstance(application)

        scope.launch(Dispatchers.IO) {
            if (skillBeanList.isNullOrEmpty()) return@launch
            for (i in 0 until skillBeanList.size) {
                Logger.e("${skillBeanList[i]}")
                if (!skillBeanList[i].needProcess) continue
                try {
                    val result = skill.runStep(skillBeanList[i], skillBeanList[i].step)
                    //如果有下个任务，则需要判断本次任务的返回结果是否需要交给下个任务
                    if (i + 1 < skillBeanList.size) {
                        Skill.findDependParam(result, skillBeanList[i + 1])
                    }
                    skillBeanList[i].result.putAll(result)
                    skillBeanList[i].resultType = SkillBean.RESULT_SUCCESS

                    callback.onFinish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    skillBeanList[i].resultType = SkillBean.RESULT_FAIL
                    skillBeanList[i].result["Error"] = "$e ${e.cause} ${e.stackTrace}"
                }
            }

            launch(Dispatchers.Default) {
                callback.onAllFinish()
                startActivity(Intent(this@AssistantService, FloatingActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    inner class AssistantServiceBinder : Binder() {
        fun getService(): AssistantService {
            return this@AssistantService
        }
    }
}