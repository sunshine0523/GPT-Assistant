package io.sunshine0523.gpt_assistant.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var dataBinding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = FragmentAboutBinding.inflate(layoutInflater)
        return dataBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = AboutFragment()
    }
}