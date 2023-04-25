package io.sunshine0523.gpt_assistant.skill

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import java.lang.StringBuilder

data class SkillBean(
    val name: String,
    val forLLM: Boolean,
    var description: String,
    val params: LinkedHashMap<String, String>,
    val step: ArrayList<Step>,
    var resultType: Int = RESULT_NULL,
    var result: LinkedHashMap<String, String>,
    //对于本条技能是否需要执行
    var needProcess: Boolean,
    //当执行列表中只有一个技能时本条技能是否可以自动执行而不用点击执行按钮
    val autoProcess: Boolean
) {
    fun getPrompt(): String? {
        val gson = GsonBuilder().addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                if (f.name == "step" || f.name == "forLLM" || f.name == "resultType" || f.name == "needProcess" || f.name == "autoProcess") return true
                return false
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return false
            }

        }).create()
        return gson.toJson(this)
    }

    fun copy(): SkillBean {
        val newStep = ArrayList<Step>()
        step.forEach {
            newStep.add(it.copy())
        }
        return SkillBean(name, forLLM, description, params.clone() as LinkedHashMap<String, String>, newStep, resultType, result.clone() as LinkedHashMap<String, String>, needProcess, autoProcess)
    }

    companion object {
        const val RESULT_NULL = 0
        const val RESULT_SUCCESS = 1
        const val RESULT_FAIL = 2
    }
}

data class Step(
    val type: Int,
    val name: String,
    //如果是operation则需要
    val functionName: String,
    val params: LinkedHashMap<String, String>
){
    companion object {
        //表示这一步骤中用到的是skill还是operation
        const val TYPE_SKILL = 0
        const val TYPE_OPERATION = 1
    }

    fun copy(): Step {
        return Step(type, name, functionName, params.clone() as LinkedHashMap<String, String>)
    }
}