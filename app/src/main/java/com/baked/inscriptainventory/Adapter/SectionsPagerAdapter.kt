package com.baked.inscriptainventory.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.baked.inscriptainventory.Fragment.*
private const val TAG = "InscriptaInventory_SPA"

class SectionsPagerAdapter(
    private val invItems: MutableList<MutableList<MutableList<String>>>,
    private var tabTitles: MutableList<String>,
    private val images: MutableList<String>,
    fm: FragmentManager
)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> Fragment0(invItems[position], images)
            1 -> Fragment1(invItems[position], images)
            2 -> Fragment2(invItems[position], images)
            3 -> Fragment3(invItems[position], images)
            4 -> Fragment4(invItems[position], images)
            5 -> Fragment5(invItems[position], images)
            6 -> Fragment6(invItems[position], images)
            7 -> Fragment7(invItems[position], images)
            8 -> Fragment8(invItems[position], images)
            9 -> Fragment9(invItems[position], images)
            10 -> Fragment10(invItems[position], images)

            else -> Fragment11(invItems[position], images)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return tabTitles.size
    }
}