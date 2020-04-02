package com.baked.inscriptainventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "InscriptaInventory_FF"
private lateinit var recyclerView: RecyclerView
private lateinit var itemsContainer: MutableList<MutableList<String>>

class FirstFragment(private val items: MutableList<MutableList<String>>) : Fragment() {
    private lateinit var rootView: View
    object SetAdapterFromActivity {
        operator fun invoke(index: String, value: String) {
            itemsContainer[index.toInt()][5] = value
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
    init{
    itemsContainer = items
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_first, container, false)
        recyclerView = rootView.findViewById(R.id.bk_rv)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
            val intent = Intent(activity, ItemActionActivity::class.java)
            intent.putExtra("Image", itemsContainer[position][1])
            intent.putExtra("PartNum", itemsContainer[position][2])
            intent.putExtra("Item", itemsContainer[position][3])
            intent.putExtra("MinStockLevel", itemsContainer[position][4])
            intent.putExtra("InStock", itemsContainer[position][5])
            intent.putExtra("Sheet", "1")
            intent.putExtra("Row", (position + 2).toString())
            intent.putExtra("FromActivity", "Fragment")

            startActivityForResult(intent, 1)
        }
        val listener = { i: Int -> fragClickListener(i) }
        recyclerView.adapter = activity?.applicationContext?.let { InventoryAdapter( itemsContainer, it,  listener) }
        recyclerView.addItemDecoration(DividerItemDecoration(activity?.applicationContext, DividerItemDecoration.VERTICAL))

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val indexStr = data?.getStringExtra("index")
                val valueStr = data?.getStringExtra("newValue")
                itemsContainer[indexStr!!.toInt()][5] = valueStr.toString()
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}
