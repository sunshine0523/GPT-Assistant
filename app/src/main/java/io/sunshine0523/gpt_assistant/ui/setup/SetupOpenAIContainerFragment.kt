package io.sunshine0523.gpt_assistant.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.orhanobut.logger.Logger
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.databinding.FragmentSetupOpenaiContainerBinding

class SetupOpenAIContainerFragment : Fragment() {

    private lateinit var dataBinding: FragmentSetupOpenaiContainerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = FragmentSetupOpenaiContainerBinding.inflate(layoutInflater)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOpenAITabAndViewPager()
    }

    private fun initOpenAITabAndViewPager() {
        val tabs = listOf(getString(R.string.setup_use_openai), getString(R.string.setup_use_azure))
        dataBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return tabs.size
            }

            override fun createFragment(position: Int): Fragment {
                return SetupOpenAIFragment.newInstance(tabs[position])
            }
        }

        TabLayoutMediator(dataBinding.tabLayout, dataBinding.viewPager, true) { tab, position -> tab.text = tabs[position] }.attach()
    }
}