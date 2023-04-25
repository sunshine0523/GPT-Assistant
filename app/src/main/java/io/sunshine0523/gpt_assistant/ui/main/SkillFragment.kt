package io.sunshine0523.gpt_assistant.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import io.sunshine0523.gpt_assistant.databinding.FragmentSkillBinding
import io.sunshine0523.gpt_assistant.skill.Skill
import io.sunshine0523.gpt_assistant.skill.SkillBean
import io.sunshine0523.gpt_assistant.ui.floating.FloatingActivity

class SkillFragment : Fragment() {

    private lateinit var dataBinding: FragmentSkillBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = FragmentSkillBinding.inflate(layoutInflater)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val skill = Skill.getInstance(requireActivity().application)
        initRecyclerView(skill.getSkillBeanMap())

        dataBinding.fabStartAssistant.setOnClickListener {
            startActivity(Intent(requireContext(), FloatingActivity::class.java))
        }
    }

    private fun initRecyclerView(skillBeanMap: HashMap<String, SkillBean>) {
        dataBinding.recyclerView.apply {
            adapter = SkillRecyclerViewAdapter(ArrayList(skillBeanMap.values))
            layoutManager = LinearLayoutManager(context)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SkillFragment()
    }
}