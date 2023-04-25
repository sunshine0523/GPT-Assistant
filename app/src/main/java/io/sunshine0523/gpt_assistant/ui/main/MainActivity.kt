package io.sunshine0523.gpt_assistant.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.databinding.ActivityMainBinding
import io.sunshine0523.gpt_assistant.ui.floating.FloatingActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dataBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        dataBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        setSupportActionBar(dataBinding.toolbar)

        val fragmentList = ArrayList<Fragment>().apply {
            add(SkillFragment.newInstance())
            add(AboutFragment.newInstance())
        }

        dataBinding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int {
                    return fragmentList.size
                }

                override fun createFragment(position: Int): Fragment {
                    return fragmentList[position]
                }

            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    dataBinding.navView.menu.getItem(position).isChecked = true
                }
            })
            offscreenPageLimit = fragmentList.size
        }

        dataBinding.navView.apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.skills -> {
                        dataBinding.viewPager.currentItem = 0
                    }
                    else -> {
                        dataBinding.viewPager.currentItem = 1
                    }
                }
                true
            }
        }
    }
}
