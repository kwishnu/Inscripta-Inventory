package com.baked.inscriptainventory

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2,
        R.string.tab_text_3
)
private var InventoryTab1: MutableList<MutableList<String>> = ArrayList()
private var InventoryTab2: MutableList<MutableList<String>> = ArrayList()
private var InventoryTab3: MutableList<MutableList<String>> = ArrayList()
private var loaded: Boolean = false
//removeAt(index: Int)
class SectionsPagerAdapter(private val invItems: MutableList<MutableList<String>>, private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            if (!loaded) {
                for (i in 0 until invItems.size) {
                    when (invItems[i][0]) {
                        "1" -> {
//                            Log.d("Inventory", invItems.toString())
                            InventoryTab1.add(invItems[i])
                        }
                        "2" -> InventoryTab2.add(invItems[i])
                        else -> InventoryTab3.add(invItems[i])
                    }
                }
                loaded = true
            }
            return when (position) {
            0 -> FirstFragment(InventoryTab1)
            1 -> SecondFragment(InventoryTab2)
            else -> ThirdFragment(InventoryTab3)
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