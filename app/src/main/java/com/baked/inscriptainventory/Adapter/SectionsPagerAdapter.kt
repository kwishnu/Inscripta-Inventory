package com.baked.inscriptainventory.Adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.baked.inscriptainventory.Fragment.FirstFragment
import com.baked.inscriptainventory.Fragment.SecondFragment
import com.baked.inscriptainventory.Fragment.ThirdFragment
import com.baked.inscriptainventory.R

private const val TAG = "InscriptaInventory_SPA"

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)
private var InventoryTab1: MutableList<MutableList<String>> = ArrayList()
private var InventoryTab2: MutableList<MutableList<String>> = ArrayList()
private var InventoryTab3: MutableList<MutableList<String>> = ArrayList()
private var loaded: Boolean = false

class SectionsPagerAdapter(
    private val invItems: MutableList<MutableList<String>>,
    private val context: Context,
    fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            if (!loaded) {
                for (i in 0 until invItems.size) {
                    when (invItems[i][0]) {
                        "1" -> InventoryTab1.add(invItems[i])
                        "2" -> InventoryTab2.add(invItems[i])
                        else -> InventoryTab3.add(invItems[i])
                    }
                }
                loaded = true
            }

            return when (position) {
            0 -> FirstFragment(
                InventoryTab1
            )
            1 -> SecondFragment(
                InventoryTab2
            )
            else -> ThirdFragment(
                InventoryTab3
            )
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 3 tabs
        return 3
    }
}