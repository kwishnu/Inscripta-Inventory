package com.baked.inscriptainventory.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.baked.inscriptainventory.Fragment.*
private const val TAG = "InscriptaInventory_SPA"

class SectionsPagerAdapter(
    private val invItems: MutableList<MutableList<MutableList<String>>>,
    private var tabTitles: MutableList<String>,
    fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> Fragment0(invItems[position])
            1 -> Fragment1(invItems[position])
            2 -> Fragment2(invItems[position])
            3 -> Fragment3(invItems[position])
            4 -> Fragment4(invItems[position])
            5 -> Fragment5(invItems[position])
            6 -> Fragment6(invItems[position])
            7 -> Fragment7(invItems[position])
            8 -> Fragment8(invItems[position])
            9 -> Fragment9(invItems[position])
            10 -> Fragment10(invItems[position])

            else -> Fragment11(invItems[position])
        }


    }

//    override fun getItemPosition(`object`: Any): Int {
//        return PagerAdapter.POSITION_NONE
//    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return tabTitles.size
    }
}