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
            11 -> Fragment11(invItems[position], images)
            12 -> Fragment12(invItems[position], images)
            13 -> Fragment13(invItems[position], images)
            14 -> Fragment14(invItems[position], images)
            15 -> Fragment15(invItems[position], images)
            16 -> Fragment16(invItems[position], images)
            17 -> Fragment17(invItems[position], images)
            18 -> Fragment18(invItems[position], images)
            19 -> Fragment19(invItems[position], images)
            20 -> Fragment20(invItems[position], images)
            21 -> Fragment21(invItems[position], images)
            22 -> Fragment22(invItems[position], images)
            23 -> Fragment23(invItems[position], images)

            else -> Fragment24(invItems[position], images)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }

    override fun getCount(): Int {
        return tabTitles.size
    }
}