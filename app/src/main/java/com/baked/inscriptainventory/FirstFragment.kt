package com.baked.inscriptainventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
private const val TAG = "InscriptaInventory_FF"

class FirstFragment(private val items: MutableList<MutableList<String>>) : Fragment() {
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private var index: String = ""
    private var value: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_first, container, false)
        recyclerView = rootView.findViewById(R.id.bk_rv)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
            val intent = Intent(activity, ItemActionActivity::class.java)
            intent.putExtra("Image", items[position][1])
            intent.putExtra("PartNum", items[position][2])
            intent.putExtra("Item", items[position][3])
            intent.putExtra("MinStockLevel", items[position][4])
            intent.putExtra("InStock", items[position][5])
            intent.putExtra("Sheet", "1")
            intent.putExtra("Row", (position + 2).toString())

            startActivityForResult(intent, 1)
        }
        val listener = { i: Int -> fragClickListener(i) }
        recyclerView.adapter = activity?.applicationContext?.let { InventoryAdapter( items, it,  listener) }
        recyclerView.addItemDecoration(DividerItemDecoration(activity?.applicationContext, DividerItemDecoration.VERTICAL))
//Log.d(TAG, items.toString())
        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val indexStr = data?.getStringExtra("index")
                val valueStr = data?.getStringExtra("newValue")
                items[indexStr!!.toInt()][5] = valueStr.toString()
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}
