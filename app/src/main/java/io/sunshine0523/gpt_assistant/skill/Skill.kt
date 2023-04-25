package io.sunshine0523.gpt_assistant.skill

import android.app.Application
import android.os.Looper
import android.widget.Toast
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.operation.AccessibilityOperation
import io.sunshine0523.gpt_assistant.operation.InternetOperation
import io.sunshine0523.gpt_assistant.operation.LLMOperation
import io.sunshine0523.gpt_assistant.operation.Operation
import io.sunshine0523.gpt_assistant.operation.OpenAppOperation
import io.sunshine0523.gpt_assistant.operation.OperationCallback
import io.sunshine0523.gpt_assistant.operation.ScheduleOperation
import kotlinx.coroutines.delay
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.lang.reflect.Method
import java.util.LinkedHashMap
import java.util.regex.Pattern

class Skill private constructor(private val application: Application) {
    private val operationMap: HashMap<String, Operation> = HashMap()
    private val skillBeanMap: HashMap<String, SkillBean> = HashMap()

    init {
        loadAssetsSkill()
        loadOperation()
    }

    fun getOperationMap(): HashMap<String, Operation> {
        return HashMap(operationMap)
    }

    fun getOperation(operationName: String): Operation? {
        return operationMap[operationName]
    }

    fun getSkillBeanMap(): HashMap<String, SkillBean> {
        val newSkillBeanMap = HashMap<String, SkillBean>()
        skillBeanMap.forEach { (k, v)->
            newSkillBeanMap[k] = v.copy()
        }
        return newSkillBeanMap
    }

    private fun loadAssetsSkill() {
        try {
            val skillPathList = application.assets.list(assetsSkillsPath)
            skillPathList?.forEach { skillPath ->
                val skillList = application.assets.list("$assetsSkillsPath/$skillPath")
                skillList?.forEach { file ->
                    if (file.endsWith(".json")) {
                        try {
                            val inputStream = application.assets.open("$assetsSkillsPath/$skillPath/$file")
                            val bytes = ByteArray(inputStream.available())
                            inputStream.read(bytes)
                            val skillInfoStr = String(bytes)
                            val skillBean = Gson().fromJson(skillInfoStr, SkillBean::class.java)
                            skillBeanMap[skillBean.name] = skillBean
                        } catch (e: Exception) {
                            Logger.e("Load skill $skillPath failed, $e")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("$e")
        }

    }

    private fun loadOperation() {
        operationMap.apply {
            put("OpenAppOperation", OpenAppOperation(application))
            put("AccessibilityOperation", AccessibilityOperation())
            put("InternetOperation", InternetOperation())
            put("LLMOperation", LLMOperation(application))
            put("ScheduleOperation", ScheduleOperation(application))
        }
    }

    suspend fun runStep(skill: SkillBean?, steps: List<Step>?): LinkedHashMap<String, String> {
        //该skill执行的结果集合
        val result = LinkedHashMap<String, String>()
        if (skill == null) return result
        steps?.forEach { step ->
            loadStepParams(skill, step)
            if (step.type == Step.TYPE_SKILL) {
                //这步操作中用到的skill
                val readOnlyChildSkill = skillBeanMap[step.name] ?: return result
                val childSkill = readOnlyChildSkill.copy()
                //将该步骤中的已知参数赋给它用到的skill
                step.params.forEach { (name, value) ->
                    childSkill.params[name] = value
                }
                result.putAll(runStep(childSkill, childSkill.step))
            } else if (step.type == Step.TYPE_OPERATION) {
                //这步操作中用到的operation
                val operation = operationMap[step.name]
                    ?: throw NullPointerException("Can not find an operation named ${step.name}")

                //寻找对应函数
                val methods = operation::class.java.methods
                var targetMethod: Method? = null
                methods.forEach { method ->
                    if (method.name == step.functionName && method.parameterCount == step.params.size + 1) {
                        targetMethod = method
                    }
                }
                if (targetMethod == null) throw NullPointerException("Can not find targetMethod ${step.functionName}")

                val params = step.params.values.toTypedArray()

                //执行并等待结果
                var isFinish = false
                targetMethod?.invoke(operation, object : OperationCallback {
                    override fun onFinish(execResult: Boolean, operationResult: LinkedHashMap<String, String>) {
                        if (!execResult) throw RuntimeException("Method $targetMethod exec failed, stop")
                        if (operationResult.isNotEmpty()) {
                            result.putAll(operationResult)
                        }
                        isFinish = true
                    }
                }, *params)

                //至多等待任务10s，但是最少等待任务2s，为了任务和任务之间的间隔
                var count = 100
                while ((!isFinish && count >= 0) || count > 100-20) {
                    delay(100)
                    --count
                }
                if (count <= 0) throw RuntimeException("Method $targetMethod exec over time, stop")
            }
        }
        return result
    }

    private fun loadStepParams(skill: SkillBean, step: Step) {
        val matcher = Pattern.compile("\\{.*\\}")
        step.params.forEach { (key, value) ->
            if (matcher.matcher(value).find()) {
                val pName = value.replace("{", "").replace("}", "")
                step.params[key] = skill.params[pName] ?: ""
            }
        }
    }

    companion object {
        private const val assetsSkillsPath = "skills"

        private var instance: Skill? = null

        fun getInstance(application: Application): Skill {
            if (instance != null) return instance!!
            return Skill(application)
        }

        /**
         * 寻找返回结果中是否有skill需要的依赖参数
         */
        fun findDependParam(result: HashMap<String, String>, skill: SkillBean) {
            skill.params.forEach { (key, value) ->
                val matcher = Pattern.compile("<.*>")
                //<dependResult>表示需要上一步结果中dependResult的值
                if (matcher.matcher(value).find()) {
                    val dependKey = value.replace("<", "").replace(">", "")
                    if (result.containsKey(dependKey)) skill.params[key] = result[dependKey]!!
                }
            }
        }

        fun freeInstance() {
            instance?.skillBeanMap?.clear()
            instance?.operationMap?.clear()
            instance = null
        }
    }
}