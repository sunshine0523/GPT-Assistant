package io.sunshine0523.gpt_assistant.ui.floating

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import com.permissionx.guolindev.PermissionX
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.service.AzureOpenAiService
import com.theokanning.openai.service.OpenAiService
import io.sunshine0523.gpt_assistant.Api
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.databinding.ActivityFloatingBinding
import io.sunshine0523.gpt_assistant.service.AssistantService
import io.sunshine0523.gpt_assistant.skill.ExecSkillCallback
import io.sunshine0523.gpt_assistant.skill.LLMResponseSkillBean
import io.sunshine0523.gpt_assistant.skill.LLMResponseSkillsBean
import io.sunshine0523.gpt_assistant.skill.RecallBean
import io.sunshine0523.gpt_assistant.skill.Skill
import io.sunshine0523.gpt_assistant.skill.SkillBean
import io.sunshine0523.gpt_assistant.utils.LLMUtils
import io.sunshine0523.gpt_assistant.view.LoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.regex.Pattern


class FloatingActivity : AppCompatActivity() {
    private lateinit var dataBinding: ActivityFloatingBinding

    private val scope = MainScope()

    private lateinit var skill: Skill
    private lateinit var toExecSkillBeanList: MutableLiveData<ArrayList<SkillBean>>
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityFloatingBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        skill = Skill.getInstance(application)
        toExecSkillBeanList = MutableLiveData()

        val openAiService: OpenAiService =
            if (Api.isAzure) {
                AzureOpenAiService(Api.key, Api.endpoint, Api.model, Duration.ZERO)
            } else {
                OpenAiService(Api.key, Duration.ZERO)
            }

        //历史记录
        val recallBeanList = ArrayList<RecallBean>()

        setFullWindow()
        requestPermission()
        initRecyclerView()
        listenInputTextChanged()
        initSpeech()

        dataBinding.fabEditText.setOnClickListener { editInputText() }
        dataBinding.fabSendMessage.setOnClickListener { sendMessage(openAiService, recallBeanList) }
        dataBinding.execSkill.setOnClickListener { execSkill() }
    }

    private fun setFullWindow() {
        val window = this.window
        window.decorView.setPadding(0, 0, 0, 0)
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = Gravity.BOTTOM
        window.attributes = lp
    }

    private fun requestPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO
            )
            .request { allGranted, _, _ ->
                if (!allGranted) {
                    val dialog = MaterialAlertDialogBuilder(this).apply {
                        setTitle(getString(R.string.tip))
                        setMessage(getString(R.string.need_permissions))
                    }.create()
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok)) {_, _ ->
                        dialog.dismiss()
                    }
                    dialog.show()
                }
            }
    }

    private fun initSpeech() {
        dataBinding.speechButton.setOnClickListener(object : View.OnClickListener {
            private var isSpeeching = false
            private var recognizer: SpeechRecognizer? = null
            private var audioInput: AudioConfig? = null

            private val content = ArrayList<String>()

            private val speechConfig = SpeechConfig.fromSubscription(Api.azureSpeechKey, Api.azureSpeechRegion).apply {
                speechRecognitionLanguage = Api.azureSpeechLanguage
            }

            @SuppressLint("SetTextI18n")
            override fun onClick(v: View) {
                if (isSpeeching) {
                    if (recognizer != null) {
                        recognizer!!.stopContinuousRecognitionAsync()
                        isSpeeching = false
                        dataBinding.speechButton.text = getString(R.string.click_to_speech)
                    } else {
                        isSpeeching = false
                    }
                    return
                }

                dataBinding.inputText.text = ""

                try {
                    content.clear()

                    audioInput = AudioConfig.fromDefaultMicrophoneInput()
                    recognizer = SpeechRecognizer(speechConfig, audioInput)

                    recognizer!!.recognizing.addEventListener { _, speechRecognitionResultEventArgs ->
                        val s = speechRecognitionResultEventArgs.result.text
                        scope.launch(Dispatchers.Main) {
                            content.add(s)
                            dataBinding.inputText.text = TextUtils.join(" ", content)
                            content.removeAt(content.size - 1)
                        }
                    }

                    recognizer!!.recognized.addEventListener {_, speechRecognitionResultEventArgs ->
                        val s = speechRecognitionResultEventArgs.result.text
                        scope.launch(Dispatchers.Main) {
                            content.add(s)
                            dataBinding.inputText.text = TextUtils.join(" ", content)
                        }
                    }

                    recognizer!!.startContinuousRecognitionAsync()
                    isSpeeching = true
                    dataBinding.speechButton.text = getString(R.string.stop_speeching)
                } catch (e: Exception) {
                    Toast.makeText(this@FloatingActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {
        val floatingRecyclerViewAdapter = FloatingRecyclerViewAdapter()
        dataBinding.recyclerView.apply {
            adapter = floatingRecyclerViewAdapter
            layoutManager = LinearLayoutManager(this@FloatingActivity)
        }
        //监听数据变化
        toExecSkillBeanList.observe(this) { skillBeanList ->
            dataBinding.execSkill.visibility = if (skillBeanList.size > 0) View.VISIBLE else View.INVISIBLE
            if (skillBeanList.size == 1 && skillBeanList[0].autoProcess) {
                execSkill()
            }
            floatingRecyclerViewAdapter.setLLMResponseSkillList(skillBeanList)
            floatingRecyclerViewAdapter.notifyDataSetChanged()
        }
    }

    private fun listenInputTextChanged() {
        dataBinding.inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable) {
                dataBinding.fabSendMessage.isEnabled = editable.isNotBlank()
            }
        })
    }

    private fun editInputText() {
        val inputText = dataBinding.inputText.text ?: ""
        val dialog = MaterialAlertDialogBuilder(this).create()
        dialog.apply {
            val editView = LayoutInflater.from(this@FloatingActivity).inflate(R.layout.view_edit_text, null, false)
            val editText: TextInputEditText = editView.findViewById(R.id.editMessage)
            editText.setText(inputText)
            setTitle(getString(R.string.edit_text))
            setView(editView)
            setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.done)) {_, _ ->
                dataBinding.inputText.text = editText.text
                dialog.dismiss()
            }
            setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) {_, _ ->
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun sendMessage(
        openAiService: OpenAiService,
        recallBeanList: ArrayList<RecallBean>
    ) {
        //判断消息是否为空
        if (dataBinding.inputText.text.isNullOrBlank()) {
            Snackbar.make(dataBinding.root, getString(R.string.please_input_some_message), Snackbar.LENGTH_SHORT).show()
        } else {
            //等待对话框
            val loadingView = LoadingView(this, getString(R.string.waiting))
            loadingView.show()

            scope.launch(Dispatchers.IO) {
                val userInput = dataBinding.inputText.text.toString()

                try {
                    val chatMessageList = LLMUtils.generatePrompt(skill.getSkillBeanMap().values.filter { it.forLLM }, userInput)
                    //向LLM发送消息
                    val completionResult = if (Api.isAzure) {
                        openAiService as AzureOpenAiService

                        openAiService.createChatCompletion(
                            ChatCompletionRequest().apply {
                                messages = chatMessageList
                                temperature = 0.7
                                maxTokens = 2000
                            },
                            Api.AZURE_VERSION_20230315
                        )
                    } else {
                        openAiService.createChatCompletion(
                            ChatCompletionRequest().apply {
                                model = Api.model
                                messages = chatMessageList
                                temperature = 0.7
                                maxTokens = 2000
                            }
                        )
                    }

                    //尝试解析结果
                    var responseText = completionResult.choices[0].message.content
                    val pattern = Pattern.compile("<\\|.*\\|>")
                    val matcher = pattern.matcher(responseText)
                    responseText = matcher.replaceAll("")
                    val result: LLMResponseSkillsBean = Gson().fromJson(responseText, LLMResponseSkillsBean::class.java)

                    //解析成功则加入到历史记录
                    recallBeanList.add(
                        RecallBean(
                        userInput,
                        result.skills)
                    )

                    launch(Dispatchers.Main) {
                        analysisLLMResult(result.skills)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch(Dispatchers.Main) {
                        Snackbar.make(dataBinding.root, "$e", Snackbar.LENGTH_SHORT).show()
                    }
                }

                launch(Dispatchers.Main) {
                    loadingView.dismiss()
                }
            }
        }
    }

    private fun analysisLLMResult(result: ArrayList<LLMResponseSkillBean>) {
        val skillList = ArrayList<SkillBean>()
        result.forEach { response ->
            var target = skill.getSkillBeanMap()[response.name]
            if (target != null) {
                target = target.copy()
                response.params.forEach { (paramName, paramValue) ->
                    target.params[paramName] = paramValue
                    target.needProcess = true
                }
                skillList.add(target)
            }
        }
        toExecSkillBeanList.value = skillList
    }

    private fun execSkill() {
        dataBinding.execSkill.apply {
            isEnabled = false
            text = getString(R.string.executing_skill)
        }
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
                val service = (binder as AssistantService.AssistantServiceBinder).getService()
                service.execSkill(toExecSkillBeanList.value, object : ExecSkillCallback {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onFinish() {
                        scope.launch(Dispatchers.Main) {
                            dataBinding.recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }

                    override fun onAllFinish() {
                        scope.launch(Dispatchers.Main) {
                            dataBinding.execSkill.apply {
                                isEnabled = true
                                text = getString(R.string.exec_skill)
                            }
                        }
                    }
                })
            }

            override fun onServiceDisconnected(p0: ComponentName) {}
        }
        bindService(Intent(this, AssistantService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::serviceConnection.isInitialized) unbindService(serviceConnection)
    }
}