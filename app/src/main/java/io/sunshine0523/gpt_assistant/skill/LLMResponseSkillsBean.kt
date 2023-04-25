package io.sunshine0523.gpt_assistant.skill

data class LLMResponseSkillsBean(
    val skills: ArrayList<LLMResponseSkillBean>
)

data class LLMResponseSkillBean(
    val name: String,
    val params: HashMap<String, String>
)