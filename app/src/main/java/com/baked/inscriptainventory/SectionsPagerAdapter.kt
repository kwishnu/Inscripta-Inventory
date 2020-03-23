package com.baked.inscriptainventory

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2,
        R.string.tab_text_3
)

class SectionsPagerAdapter(private val invItems: MutableList<MutableList<String>>, private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                FirstFragment(invItems)
            }
            1 -> {
                SecondFragment(invItems)
            }
            2 -> {
                ThirdFragment(invItems)
            }
//            else -> null
            else -> FirstFragment(invItems)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 3 tabs
        return 3
    }
}