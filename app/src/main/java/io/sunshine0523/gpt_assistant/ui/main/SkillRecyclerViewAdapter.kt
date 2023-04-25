package io.sunshine0523.gpt_assistant.ui.main

import android.content.Context
import android.content.DialogInterface
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.skill.SkillBean
import java.lang.StringBuilder

class SkillRecyclerViewAdapter(private val skillList: ArrayList<SkillBean>) : RecyclerView.Adapter<SkillRecyclerViewAdapter.ViewHolder>() {

    private lateinit var context: Context

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val touchView: View = itemView.findViewById(R.id.touch_view)
        val title: TextView = itemView.findViewById(R.id.text_title)
        val summary: TextView = itemView.findViewById(R.id.text_summary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false))
    }

    override fun getItemCount(): Int {
        return skillList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = skillList[position].name
        holder.summary.text = skillList[position].description

        holder.touchView.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(context).apply {
                setTitle(context.getString(R.string.skill_detail_info))
                setMessage(getSkillDetainInfo(skillList[position]))
            }.create()
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok))
            { _, _ -> dialog.dismiss() }
            dialog.show()
        }
    }

    private fun getSkillDetainInfo(skillBean: SkillBean): String {
        val sb = StringBuilder()
        sb.append("${context.getString(R.string.skill_name)} ${skillBean.name}\n")
        sb.append("${context.getString(R.string.skill_description)} ${skillBean.description}\n")
        sb.append("${context.getString(R.string.skill_params)} ${skillBean.params}\n")
        sb.append("${context.getString(R.string.skill_step)}\n")
        skillBean.step.forEach { step ->
            sb.append("-----\n")
            sb.append("${context.getString(R.string.step_name)} ${step.name}\n")
            sb.append("${context.getString(R.string.step_type)} ${step.type}\n")
            sb.append("${context.getString(R.string.step_invoke_func)} ${step.functionName}\n")
            sb.append("${context.getString(R.string.step_params)} ${step.params}\n")
        }
        return sb.toString()
    }
}