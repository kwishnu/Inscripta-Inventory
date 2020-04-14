package com.baked.inscriptainventory.Fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baked.inscriptainventory.Activity.DeleteItemActivity
import com.baked.inscriptainventory.Activity.EditItemActivity
import com.baked.inscriptainventory.Activity.ItemActionActivity
import com.baked.inscriptainventory.Adapter.InventoryAdapter
import com.baked.inscriptainventory.R

private const val TAG = "InscriptaInventory_IF"
private lateinit var recyclerView: RecyclerView
private lateinit var itemsContainer: MutableList<MutableList<String>>
private lateinit var sheetNum: String

class InvFragment(private val items: MutableList<MutableList<String>>) : Fragment() {//, private val tabNumber: Int
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
//            val test = sheetNum
            if (reason == "deleteItem") {
                itemsContainer.removeAt(index.toInt())
            } else if (reason == "addItem") {
                itemsContainer.add(0, mutableListOf(imageNum, partNum, itemName, minStockLevel, numInStock, "2"))//sheetNum,
            } else {
                itemsContainer[index.toInt()][0] = imageNum
                itemsContainer[index.toInt()][1] = partNum
                itemsContainer[index.toInt()][2] = itemName
                itemsContainer[index.toInt()][3] = minStockLevel
                itemsContainer[index.toInt()][4] = numInStock
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }
    init{
    itemsContainer = items
    }
//    init {
//        sheetNum = "1"//tabNumber.toString()
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        Log.d(TAG, tabNumber.toString())
        rootView = inflater.inflate(R.layout.fragment_layout, container, false)
        recyclerView = rootView.findViewById(
            R.id.rv
        )
        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
            val intent = Intent(activity, ItemActionActivity::class.java)
            intent.putExtra("Image", itemsContainer[position][0])
            intent.putExtra("PartNum", itemsContainer[position][1])
            intent.putExtra("Item", itemsContainer[position][2])
            intent.putExtra("MinStockLevel", itemsContainer[position][3])
            intent.putExtra("InStock", itemsContainer[position][4])
            intent.putExtra("Sheet", "1")//tabNumber.toString())
            intent.putExtra("Row", (position + 2).toString())
            intent.putExtra("FromActivity", "Fragment")

            startActivityForResult(intent, 1)
        }
        fun longClickListener(position: Int, view: View) {
            showPopUp(position, view)
        }
        val listener = { i: Int -> fragClickListener(i) }
        val longClickListener = { i: Int, view: View -> longClickListener(i, view) }
        recyclerView.adapter = activity?.applicationContext?.let {
            InventoryAdapter(
                itemsContainer,
                it,
                listener
//                longClickListener
            )
        }
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
                    val intent = Intent(activity, EditItemActivity::class.java)
                    intent.putExtra("Image", itemsContainer[pos][0])
                    intent.putExtra("PartNum", itemsContainer[pos][1])
                    intent.putExtra("Item", itemsContainer[pos][2])
                    intent.putExtra("MinStockLevel", itemsContainer[pos][3])
                    intent.putExtra("InStock", itemsContainer[pos][4])
                    intent.putExtra("Sheet", "1")//tabNumber.toString())
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)

                }
                R.id.header2 -> {//Go to Delete Item Activity
                    val intent = Intent(activity, DeleteItemActivity::class.java)
                    Log.d(TAG, itemsContainer[pos].toString())
                    intent.putExtra("Image", itemsContainer[pos][0])
                    intent.putExtra("PartNum", itemsContainer[pos][1])
                    intent.putExtra("Item", itemsContainer[pos][2])
                    intent.putExtra("MinStockLevel", itemsContainer[pos][3])
                    intent.putExtra("InStock", itemsContainer[pos][4])
                    intent.putExtra("Sheet", "1")//tabNumber.toString())
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)
                }
            }
            true
        }
    }
}
