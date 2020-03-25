package com.baked.inscriptainventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ThirdFragment(private val items: MutableList<MutableList<String>>) : Fragment() {
    lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private var InventoryItems: ArrayList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_third, container, false)
        recyclerView = rootView.findViewById(R.id.pp_rv)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
//            val intent = Intent(activity, List::class.java)
//            intent.putExtra("Item",  InventoryItems[position])
//            startActivity(intent)

            Toast.makeText(activity, position.toString() + " is clicked..." + "which is " + items[position], Toast.LENGTH_LONG).show()
        }
        val listener = { i: Int -> fragClickListener(i) }
        recyclerView.adapter = activity?.applicationContext?.let { InventoryAdapter( items, it,  listener) }
        recyclerView.addItemDecoration(DividerItemDecoration(activity?.applicationContext, DividerItemDecoration.VERTICAL))

        return rootView
    }
}
