package com.baked.inscriptainventory.Fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baked.inscriptainventory.Activity.DeleteItemActivity
import com.baked.inscriptainventory.Activity.EditItemActivity
import com.baked.inscriptainventory.Activity.ItemActionActivity
import com.baked.inscriptainventory.Adapter.InventoryAdapter
import com.baked.inscriptainventory.R

private const val TAG = "InscriptaInventory_SF"
private lateinit var recyclerView: RecyclerView
private lateinit var itemsContainer: MutableList<MutableList<String>>

class SecondFragment(private val items: MutableList<MutableList<String>>) : Fragment() {
    private lateinit var rootView: View
    object SetAdapterFromActivity {
        operator fun invoke( reason: String,
                             sheetNum: String,
                             index: String,
                             imageNum: String,
                             partNum: String,
                             itemName: String,
                             minStockLevel: String,
                             numInStock: String
        ) {
            if (reason == "deleteItem") {
                itemsContainer.removeAt(index.toInt())
            } else if (reason == "addItem") {
                itemsContainer.add(0, mutableListOf(sheetNum, imageNum, partNum, itemName, minStockLevel, numInStock, "2"))
            } else {
                itemsContainer[index.toInt()][1] = imageNum
                itemsContainer[index.toInt()][2] = partNum
                itemsContainer[index.toInt()][3] = itemName
                itemsContainer[index.toInt()][4] = minStockLevel
                itemsContainer[index.toInt()][5] = numInStock
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
    init{
        itemsContainer = items
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_second, container, false)
        recyclerView = rootView.findViewById(
            R.id.ir_rv
        )
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
            val intent = Intent(activity, ItemActionActivity::class.java)
            intent.putExtra("Image",  itemsContainer[position][1])
            intent.putExtra("PartNum",  itemsContainer[position][2])
            intent.putExtra("Item",  itemsContainer[position][3])
            intent.putExtra("MinStockLevel",  itemsContainer[position][4])
            intent.putExtra("InStock", itemsContainer[position][5])
            intent.putExtra("Sheet", "2")
            intent.putExtra("Row", (position + 2).toString())
            intent.putExtra("FromActivity", "Fragment")

            startActivityForResult(intent, 1)
        }
        fun longClickListener(position: Int, view: View) {
            showPopUp(position, view)
        }
        val listener = { i: Int -> fragClickListener(i) }
        val longClickListener = { i: Int, view: View -> longClickListener(i, view) }
//        recyclerView.adapter = activity?.applicationContext?.let {
//            InventoryAdapter(
//                InvFragment(items)
////                longClickListener
//            )
//        }
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

    private fun showPopUp(pos: Int, view: View) {
        val popupMenu = activity?.let { PopupMenu(it, view, 0,
            R.attr.actionOverflowMenuStyle, 0) }
        val inflater = popupMenu?.menuInflater
        inflater?.inflate(R.menu.context_menu, popupMenu.menu)
        popupMenu?.show()

        popupMenu?.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.header1 -> {//Go to Edit Item Activity
                    Log.d(TAG, itemsContainer[pos][3])
                    val intent = Intent(activity, EditItemActivity::class.java)
                    intent.putExtra("Image", itemsContainer[pos][1])
                    intent.putExtra("PartNum", itemsContainer[pos][2])
                    intent.putExtra("Item", itemsContainer[pos][3])
                    intent.putExtra("MinStockLevel", itemsContainer[pos][4])
                    intent.putExtra("InStock", itemsContainer[pos][5])
                    intent.putExtra("Sheet", "2")
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)

                }
                R.id.header2 -> {//Go to Delete Item Activity
                    Log.d(TAG, itemsContainer[pos][3])
                    val intent = Intent(activity, DeleteItemActivity::class.java)
                    intent.putExtra("Image", itemsContainer[pos][1])
                    intent.putExtra("PartNum", itemsContainer[pos][2])
                    intent.putExtra("Item", itemsContainer[pos][3])
                    intent.putExtra("MinStockLevel", itemsContainer[pos][4])
                    intent.putExtra("InStock", itemsContainer[pos][5])
                    intent.putExtra("Sheet", "2")
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)

                }
            }
            true
        }
    }
}
