package io.sunshine0523.gpt_assistant.ui.floating

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.switchmaterial.SwitchMaterial
import com.orhanobut.logger.Logger
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.skill.SkillBean
import java.lang.StringBuilder

class FloatingRecyclerViewAdapter :
    RecyclerView.Adapter<FloatingRecyclerViewAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var skillList: ArrayList<SkillBean>? = null

    fun setLLMResponseSkillList(skillList: ArrayList<SkillBean>) {
        this.skillList = skillList
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val touchView: MaterialCardView = itemView.findViewById(R.id.touch_view)
        val title: TextView = itemView.findViewById(R.id.text_title)
        val result: TextView = itemView.findViewById(R.id.text_result)
        val needProcessSwitch: SwitchMaterial = itemView.findViewById(R.id.switch_app)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_response_skill, parent, false))
    }

    override fun getItemCount(): Int {
        return skillList?.size?:0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skillBean = skillList!![position]
        holder.title.text = skillBean.name

        when (skillBean.resultType) {
            SkillBean.RESULT_NULL -> {
                holder.touchView.setCardBackgroundColor(context.getColor(R.color.surface))
            }
            SkillBean.RESULT_SUCCESS -> {
                holder.touchView.setCardBackgroundColor(context.getColor(R.color.success))
            }
            SkillBean.RESULT_FAIL -> {
                holder.touchView.setCardBackgroundColor(context.getColor(R.color.fail))
            }
        }

        val sb = StringBuilder()
        sb.append("<b>${context.getString(R.string.result)}</b><br>")
        skillBean.result.forEach {(key, value) ->
            sb.append("<b>$key: </b><br>$value<br>")
        }
        sb.append("<b>${context.getString(R.string.skill_description)}</b><br> ${skillBean.description}<br>")
        sb.append("<b>${context.getString(R.string.skill_params)}</b><br> ${skillBean.params}<br>")
        holder.result.text = Html.fromHtml(sb.toString(), 0)

        holder.needProcessSwitch.setOnCheckedChangeListener { _, b ->
            skillBean.needProcess = b
        }

        holder.touchView.setOnClickListener {
            holder.needProcessSwitch.isChecked = !holder.needProcessSwitch.isChecked
        }
    }

    private fun getSkillDetainInfo(skillBean: SkillBean): String {
        val sb = StringBuilder()
        sb.append("${context.getString(R.string.skill_name)} ${skillBean.name}\n")
        sb.append("${context.getString(R.string.skill_description)} ${skillBean.description}\n")
        sb.append("${context.getString(R.string.skill_params)} ${skillBean.params}\n")
        return sb.toString()
    }
}