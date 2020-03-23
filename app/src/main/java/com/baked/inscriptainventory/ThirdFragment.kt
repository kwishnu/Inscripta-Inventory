package com.baked.inscriptainventory

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ThirdFragment(private val items: ArrayList<String>) : Fragment() {
    lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private var InventoryItems: ArrayList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        addItems()
        rootView = inflater.inflate(R.layout.fragment_third, container, false)
        recyclerView = rootView.findViewById(R.id.pp_rv)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
//            val intent = Intent(activity, List::class.java)
//            intent.putExtra("Item",  InventoryItems[position])
//            startActivity(intent)

            Toast.makeText(activity, position.toString() + " is clicked..." + "which is " + InventoryItems[position], Toast.LENGTH_LONG).show()
        }
        val listener = { i: Int -> fragClickListener(i) }
        recyclerView.adapter = activity?.applicationContext?.let { InventoryAdapter( InventoryItems, it,  listener) }
        recyclerView.addItemDecoration(DividerItemDecoration(activity?.applicationContext, DividerItemDecoration.VERTICAL))

        return rootView
    }

    private fun addItems() {
        InventoryItems.add("amoeba")
        InventoryItems.add("paramecium")
        InventoryItems.add("snark")
        InventoryItems.add("pangolin")
        InventoryItems.add("raccoon")
        InventoryItems.add("bird")
        InventoryItems.add("snake")
        InventoryItems.add("lizard")
        InventoryItems.add("hamster")
        InventoryItems.add("bear")
        InventoryItems.add("lion")
        InventoryItems.add("tiger")
        InventoryItems.add("horse")
        InventoryItems.add("frog")
        InventoryItems.add("fish")
        InventoryItems.add("shark")
        InventoryItems.add("turtle")
        InventoryItems.add("elephant")
        InventoryItems.add("cow")
        InventoryItems.add("beaver")
        InventoryItems.add("bison")
        InventoryItems.add("porcupine")
        InventoryItems.add("rat")
        InventoryItems.add("mouse")
        InventoryItems.add("goose")
        InventoryItems.add("deer")
        InventoryItems.add("fox")
        InventoryItems.add("moose")
        InventoryItems.add("buffalo")
        InventoryItems.add("monkey")
        InventoryItems.add("penguin")
        InventoryItems.add("parrot")

//    recyclerView.adapter?.notifyDataSetChanged()

    }
}
