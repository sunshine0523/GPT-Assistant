package io.sunshine0523.gpt_assistant.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.sunshine0523.gpt_assistant.Api
import io.sunshine0523.gpt_assistant.databinding.FragmentSetupSpeechBinding
import io.sunshine0523.gpt_assistant.ui.main.MainActivity
import kotlinx.coroutines.MainScope

class SetupSpeechFragment : Fragment() {
    private val scope = MainScope()

    private lateinit var dataBinding: FragmentSetupSpeechBinding
    private lateinit var viewModel: SetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = FragmentSetupSpeechBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataBinding.azureSpeechKey.setText(viewModel.getAzureSpeechKey())
        dataBinding.azureSpeechRegion.setText(viewModel.getAzureSpeechRegion())
        dataBinding.azureSpeechLanguage.setText(viewModel.getAzureSpeechLanguage())

        dataBinding.fabDone.setOnClickListener {
            Api.azureSpeechKey = dataBinding.azureSpeechKey.text.toString()
            Api.azureSpeechRegion = dataBinding.azureSpeechRegion.text.toString()
            if (dataBinding.rememberInfo.isChecked) {
                viewModel.setAzureSpeechKey(Api.azureSpeechKey)
                viewModel.setAzureSpeechRegion(Api.azureSpeechRegion)
                viewModel.setAzureSpeechLanguage(Api.azureSpeechLanguage)
            }

            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

}