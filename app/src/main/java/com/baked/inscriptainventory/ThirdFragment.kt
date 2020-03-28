package com.baked.inscriptainventory

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
private const val TAG = "InscriptaInventory"

class ThirdFragment(private val items: MutableList<MutableList<String>>) : Fragment() {
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_third, container, false)
        recyclerView = rootView.findViewById(R.id.pp_rv)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
            val intent = Intent(activity, ItemActionActivity::class.java)
            intent.putExtra("Image",  items[position][1])
            intent.putExtra("PartNum",  items[position][2])
            intent.putExtra("Item",  items[position][3])
            intent.putExtra("MinStockLevel",  items[position][4])
            intent.putExtra("InStock",  items[position][5])
            startActivity(intent)
        }
        val listener = { i: Int -> fragClickListener(i) }
        recyclerView.adapter = activity?.applicationContext?.let { InventoryAdapter( items, it,  listener) }
        recyclerView.addItemDecoration(DividerItemDecoration(activity?.applicationContext, DividerItemDecoration.VERTICAL))

        return rootView
    }
}
