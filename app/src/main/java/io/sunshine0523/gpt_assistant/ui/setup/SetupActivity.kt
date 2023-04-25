package io.sunshine0523.gpt_assistant.ui.setup

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.permissionx.guolindev.PermissionX
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.databinding.ActivitySetupBinding
import io.sunshine0523.gpt_assistant.utils.PermissionUtils

class SetupActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivitySetupBinding
    private lateinit var accessibilityRFAR: ActivityResultLauncher<Intent>
    private lateinit var overlayRFAR: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivitySetupBinding.inflate(layoutInflater)
        dataBinding.lifecycleOwner = this
        setContentView(dataBinding.root)

        setSupportActionBar(dataBinding.toolbar)

        accessibilityRFAR = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!checkAccessibilityService()) showRequireAccessibilityPermissionDialog()
            else if (!checkOverlayPermission()) showRequireDrawOverlayPermissionDialog()
        }

        overlayRFAR = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!checkOverlayPermission()) showRequireDrawOverlayPermissionDialog()
        }
        checkRunningPermission()
    }

    private fun checkRunningPermission() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.RECORD_AUDIO
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    if (!checkAccessibilityService()) {
                        showRequireAccessibilityPermissionDialog()
                    } else if (!checkOverlayPermission()) {
                        showRequireDrawOverlayPermissionDialog()
                    }
                } else {
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

    private fun showRequireAccessibilityPermissionDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.tip))
            setMessage(getString(R.string.require_accessibility))
            setPositiveButton(getString(R.string.authorisation)) { _, _ ->
                accessibilityRFAR.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            setNegativeButton(getString(R.string.cancel)) {_, _ -> finish()}
            setCancelable(false)
        }.create().show()
    }

    private fun showRequireDrawOverlayPermissionDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.tip))
            setMessage(getString(R.string.require_overlay))
            setPositiveButton(getString(R.string.authorisation)) { _, _ ->
                overlayRFAR.launch(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
            }
            setNegativeButton(getString(R.string.cancel)) {_, _ -> finish()}
            setCancelable(false)
        }.create().show()
    }

    private fun checkAccessibilityService(): Boolean {
        return PermissionUtils.isAccessibilitySettingsOn(
            this,
            "$packageName/io.sunshine0523.gpt_assistant.service.MyAccessibilityService"
        )
    }

    private fun checkOverlayPermission(): Boolean {
        return PermissionUtils.checkOverlayPermission(this)
    }

    fun toSetupSpeech() {
        findNavController(R.id.fragment_setup).navigate(R.id.action_setup_openai_to_setup_speech)
    }
}