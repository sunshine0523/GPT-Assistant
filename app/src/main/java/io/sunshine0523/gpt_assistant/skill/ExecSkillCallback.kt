package io.sunshine0523.gpt_assistant.skill

interface ExecSkillCallback {
    fun onFinish()
    fun onAllFinish()
}