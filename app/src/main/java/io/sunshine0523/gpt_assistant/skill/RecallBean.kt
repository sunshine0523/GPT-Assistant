package io.sunshine0523.gpt_assistant.skill

data class RecallBean(
    val userInput: String,
    val usedSkills: ArrayList<LLMResponseSkillBean>
)