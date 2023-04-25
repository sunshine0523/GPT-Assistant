package io.sunshine0523.gpt_assistant.utils

import android.annotation.SuppressLint
import com.orhanobut.logger.Logger
import com.theokanning.openai.completion.chat.ChatMessage
import io.sunshine0523.gpt_assistant.skill.RecallBean
import io.sunshine0523.gpt_assistant.skill.SkillBean
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.StringBuilder

object LLMUtils {
    @SuppressLint("SimpleDateFormat")
    fun generatePrompt(skillBeanList: List<SkillBean>, userInput: String, recallList: List<RecallBean>): String {
        val sb = StringBuilder()
        sb.append("You are a smartphone assistant\n\n")
        sb.append("Today is ${SimpleDateFormat("yyyy-MM-dd hh:mm").format(Date())}\n\n")
        sb.append("Between [SKILLS BEGIN] and [SKILLS END] are all skills that you have. Use description to select skills. Paying attention to adhering to the restrictions of use in the description\n\n")
        sb.append("[SKILLS BEGIN]\n")
        skillBeanList.forEach { skill ->
            sb.append(skill.getPrompt()).append("\n")
        }
        sb.append("[SKILLS END]\n\n")
        sb.append("Every skills has amount of cost, try to choose the least skills\n\n")
        sb.append("Below [USER INPUT] is user`s input. You must choice some skills with user`s input. Remember filled skill`s param`s {value}. If you do not find skills with input. just return {\"skills\":[]}\n\n")
        sb.append("[USER INPUT]\n")
        sb.append(userInput).append("\n\n")
//        sb.append("Below [RECALL] is recall. You can remember what do you do previously\n\n")
//        sb.append("[RECALL]\n")
//        sb.append(Gson().toJson(recallList)).append("\n\n")
        sb.append("There are some example for how to generate response:\n\n")
        sb.append("[EXAMPLE 1]\n")
        sb.append("[USER INPUT]\n总结一下www.baidu.com的内容\n")
        //sb.append("YOUR THINK: I should to use skill `Access Web` to get the content of url. Then fill result to `webContent`. Then I should to use skill `Summarize text` and I should to fill param `text` with `{webContent}`. Because I want to summarize it\n")
        sb.append("{\"skills\":[{\"name\":\"Access Web\",\"params\":{\"webUrl\":\"www.baidu.com\"}},{\"name\":\"Summarize Text\",\"params\":{\"text\":\"<webContent>\"}}]}\n")
        sb.append("[EXAMPLE 2\n")
        sb.append("[USER INPUT]\nWhat is bear?\n")
        sb.append("{\"skills\":[{\"name\":\"Knowledge QA\",\"params\":{\"question\":\"What is bear?\"}}]}\n\n")
        sb.append("You should only respond in JSON format as described below. Ensure the response can be parsed by Java Gson().fromJson()\n\n")
        sb.append("{\"skills\":[{\"name\":\"skillName1\",\"params\":{\"paramName1\":\"value\"}, \"result\":{\"resultName\":\"{value}\"}},{\"name\":\"skillName2\",\"params\":{\"paramName3\":\"<resultName>\",\"paramName4\":\"value\"}}]}\n\n")
//        sb.append("Using the format specified above:\n")
        return sb.toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun generatePrompt(skillBeanList: List<SkillBean>, userInput: String): ArrayList<ChatMessage> {
        val chatMessageList = ArrayList<ChatMessage>()
        val systemChatMessage = ChatMessage().apply { role = "system" }
        val userChatMessage = ChatMessage().apply { role = "user" }

        val sb = StringBuilder()
        sb.append("You are a smartphone assistant\n")
        sb.append("Today is ${SimpleDateFormat("yyyy-MM-dd").format(Date())}\n")
        sb.append("Between [SKILLS BEGIN] and [SKILLS END] are all skills that you have\n")
        sb.append("[SKILLS BEGIN]\n")
        skillBeanList.forEach { skill ->
            sb.append(skill.getPrompt()).append("\n")
        }
        sb.append("[SKILLS END]\n\n")
        sb.append("There are some example for how to generate response:\n\n")
        sb.append("[EXAMPLE 1]\n")
        sb.append("[USER INPUT]\n总结www.example.com的内容\n")
        sb.append("{\"skills\":[{\"name\":\"Access Web\",\"params\":{\"webUrl\":\"www.example.com\"}},{\"name\":\"Summarize Text\",\"params\":{\"text\":\"<webContent>\"}}]}\n")
        sb.append("Below [USER INPUT] is user`s input. You must choice some skills with user`s input. Remember filled skill`s param`s {value}. If you do not find skills with input. just return {\"skills\":[]}\n")
        sb.append("[USER INPUT]\n")

        val userSb = StringBuilder()
        userSb.append("Every skills has amount of cost. Try to choose the least skills\n")
        userSb.append("You should only respond in JSON format as described below. Ensure the response can be parsed by Java Gson().fromJson()\n")
        userSb.append("{\"skills\":[{\"name\":\"skillName1\",\"params\":{\"paramName1\":\"value\"}, \"result\":{\"resultName\":\"{value}\"}},{\"name\":\"skillName2\",\"params\":{\"paramName3\":\"<resultName>\",\"paramName4\":\"value\"}}]}\n")
        userSb.append(userInput)
        systemChatMessage.content = sb.toString()
        userChatMessage.content = userSb.toString()

        chatMessageList.apply {
            add(systemChatMessage)
            add(userChatMessage)
        }
        return chatMessageList
    }
}