package io.sunshine0523.gpt_assistant.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.orhanobut.logger.Logger
import com.theokanning.openai.service.AzureOpenAiService
import com.theokanning.openai.service.OpenAiService
import io.sunshine0523.gpt_assistant.Api
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.databinding.FragmentSetupOpenaiBinding
import io.sunshine0523.gpt_assistant.view.LoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Duration

private const val ARG_TYPE = "type"

class SetupOpenAIFragment : Fragment() {
    private var type: String = ""

    private val scope = MainScope()

    private lateinit var dataBinding: FragmentSetupOpenaiBinding
    private lateinit var viewModel: SetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(ARG_TYPE) ?: getString(R.string.setup_use_openai)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = FragmentSetupOpenaiBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (type) {
            getString(R.string.setup_use_openai) -> {
                dataBinding.endpoint.visibility = View.GONE
                Api.isAzure = false
            }
            getString(R.string.setup_use_azure) -> Api.isAzure = true
        }

        fillSaveInfo()

        dataBinding.fabDone.setOnClickListener {
            Api.key = dataBinding.openAIKey.text?.toString() ?: ""
            Api.model = dataBinding.model.text?.toString() ?: ""
            Api.endpoint = dataBinding.endpoint.text?.toString() ?: Api.OPENAI_BASE_URL

            validOpenAI()
        }
    }

    private fun validOpenAI() {
        val loadingView = LoadingView(requireContext(), getString(R.string.waiting))
        loadingView.show()
        scope.launch(Dispatchers.IO) {
            if (Api.isAzure) {
                try {
                    val service = AzureOpenAiService(Api.key, Api.endpoint, Api.model, Duration.ZERO)
                    service.getDeployment(Api.model, Api.AZURE_VERSION_20221201)
                    if (dataBinding.rememberInfo.isChecked) saveInfo()
                    else clearInfo()

                    launch(Dispatchers.Main) {
                        loadingView.dismiss()
                        (requireActivity() as SetupActivity).toSetupSpeech()
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        loadingView.dismiss()
                        e.printStackTrace()
                        Toast.makeText(requireContext(), getString(R.string.access_openai_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                try {
                    val service = OpenAiService(Api.key)
                    service.listModels()
                    if (dataBinding.rememberInfo.isChecked) saveInfo()
                    else clearInfo()

                    launch(Dispatchers.Main) {
                        loadingView.dismiss()
                        (requireActivity() as SetupActivity).toSetupSpeech()
                    }
                } catch (e: Exception) {
                    Logger.e("$e")
                    launch(Dispatchers.Main) {
                        loadingView.dismiss()
                        Toast.makeText(requireContext(), getString(R.string.access_openai_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun fillSaveInfo() {
        if (Api.isAzure) {
            dataBinding.openAIKey.setText(viewModel.getAzureKey())
            dataBinding.model.setText(viewModel.getAzureDeployment())
            dataBinding.endpoint.setText(viewModel.getAzureEndpoint())
        } else {
            dataBinding.openAIKey.setText(viewModel.getOpenAIKey())
            dataBinding.model.setText(viewModel.getOpenAIModel())
        }
    }

    private fun saveInfo() {
        if (Api.isAzure) {
            viewModel.setAzureKey(Api.key)
            viewModel.setAzureDeployment(Api.model)
            viewModel.setAzureEndpoint(Api.endpoint)
        } else {
            viewModel.setOpenAIKey(Api.key)
            viewModel.setOpenAIModel(Api.model)
        }
    }

    private fun clearInfo() {
        if (Api.isAzure) {
            viewModel.setAzureKey("")
            viewModel.setAzureDeployment("")
            viewModel.setAzureEndpoint("")
        } else {
            viewModel.setOpenAIKey("")
            viewModel.setOpenAIModel("")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            SetupOpenAIFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
    }
}