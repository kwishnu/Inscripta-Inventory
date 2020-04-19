package com.baked.inscriptainventory.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
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
import com.baked.inscriptainventory.Resource.CallServer
import kotlinx.android.synthetic.main.fragment_layout.*
import com.baked.inscriptainventory.Activity.MainActivity.Companion.globalIPAddress
private const val TAG = "InscriptaInventory_F9"
private const val TAB = "10"
private lateinit var recyclerView: RecyclerView
private lateinit var itemsContainer: MutableList<MutableList<String>>


class Fragment9(private val items: MutableList<MutableList<String>>) : Fragment() {
    private lateinit var rootView: View
    object SetAdapterFromActivity {
        operator fun invoke(
            reason: String,
            index: String,
            imageNum: String,
            partNum: String,
            itemName: String,
            minStockLevel: String,
            numInStock: String,
            commentStr: String
        ) {
            if (reason == "deleteItem") {
                itemsContainer.removeAt(index.toInt())
            } else if (reason == "addItem") {
                itemsContainer.add(0, mutableListOf(imageNum, partNum, itemName, minStockLevel, numInStock, commentStr, "2"))
            } else {
                itemsContainer[index.toInt()][0] = imageNum
                itemsContainer[index.toInt()][1] = partNum
                itemsContainer[index.toInt()][2] = itemName
                itemsContainer[index.toInt()][3] = minStockLevel
                itemsContainer[index.toInt()][4] = numInStock
                itemsContainer[index.toInt()][5] = commentStr
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    init {
        itemsContainer = items
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_layout, container, false)
        recyclerView = rootView.findViewById(
            R.id.rv
        )

        recyclerView.layoutManager = LinearLayoutManager(activity)
        fun fragClickListener(position: Int) {
            launchItemActionActivity(position)
        }

        fun imageClickListener(position: Int) {
            if (items[position][5].isEmpty() || items[position][5] == "null") {
                launchItemActionActivity(position)
            } else {
                showEditableComment(position)
            }
        }

        fun longClickListener(position: Int, view: View) {
            showPopUp(position, view)
        }

        val listener = { i: Int -> fragClickListener(i) }
        val imageListener = { i: Int -> imageClickListener(i) }
        val longClickListener = { i: Int, view: View -> longClickListener(i, view) }
        recyclerView.adapter = activity?.applicationContext?.let {
            InventoryAdapter(
                items,
                it,
                listener,
                imageListener,
                longClickListener
            )
        }
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                activity?.applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }

    private fun launchItemActionActivity(pos: Int) {
        val intent = Intent(activity, ItemActionActivity::class.java)
        intent.putExtra("Image", items[pos][0])
        intent.putExtra("PartNum", items[pos][1])
        intent.putExtra("Item", items[pos][2])
        intent.putExtra("MinStockLevel", items[pos][3])
        intent.putExtra("InStock", items[pos][4])
        intent.putExtra("Sheet", "10")
        intent.putExtra("Row", (pos + 2).toString())
        intent.putExtra("Comment", items[pos][5])
        intent.putExtra("FromActivity", "Fragment")

        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val indexStr = data?.getStringExtra("index")
                val valueStr = data?.getStringExtra("newValue")
                items[indexStr!!.toInt()][4] = valueStr.toString()
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showPopUp(pos: Int, view: View) {
        val popupMenu = activity?.let {
            PopupMenu(
                it, view, 0,
                R.attr.actionOverflowMenuStyle, 0
            )
        }
        val inflater = popupMenu?.menuInflater
        inflater?.inflate(R.menu.context_menu, popupMenu.menu)
        popupMenu?.show()

        popupMenu?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.header1 -> {//Go to Edit Item Activity
                    val intent = Intent(activity, EditItemActivity::class.java)
                    intent.putExtra("Image", items[pos][0])
                    intent.putExtra("PartNum", items[pos][1])
                    intent.putExtra("Item", items[pos][2])
                    intent.putExtra("MinStockLevel", items[pos][3])
                    intent.putExtra("InStock", items[pos][4])
                    intent.putExtra("Sheet", "10")
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("Comment", items[pos][5])
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)
                }
                R.id.header2 -> {//Go to Delete Item Activity
                    val intent = Intent(activity, DeleteItemActivity::class.java)
                    intent.putExtra("Image", items[pos][0])
                    intent.putExtra("PartNum", items[pos][1])
                    intent.putExtra("Item", items[pos][2])
                    intent.putExtra("MinStockLevel", items[pos][3])
                    intent.putExtra("InStock", items[pos][4])
                    intent.putExtra("Sheet", "10")
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("Comment", items[pos][5])
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)
                }
            }
            true
        }
    }

    @SuppressLint("InflateParams")
    fun showEditableComment(pos: Int) {
        val view: View = layoutInflater.inflate(R.layout.dialog_edit_comment, null);
        val etComment = view.findViewById<View>(R.id.et_comment) as EditText
        var clicked = false
        etComment.setText(items[pos][5])
        etComment.setOnFocusChangeListener{ _: View, focused: Boolean ->
            if (focused) clicked = true
        }
        val dialogBuilder = context?.let { AlertDialog.Builder(it) }
        dialogBuilder
            ?.setMessage("")
            ?.setCancelable(true)
            ?.setView(view)
            ?.setPositiveButton("Save Edit", DialogInterface.OnClickListener { dialog, _ ->
                if (clicked){
                    items[pos][5] = etComment.text.toString()
                    recyclerView.adapter?.notifyDataSetChanged()
                    context?.let {
                        CallServer(it).makeCall(
                            rv,//View
                            globalIPAddress,//ipAddressStr
                            "editComment",//Reason
                            "none",//Count
                            "none",//Part Number
                            "none",//Image
                            TAB,//Sheet
                            items[pos][6],//Row Number
                            "false",//Send Warning
                            "none",//Item Name
                            "none",//Min Stock Level
                            etComment.text.toString()//Comment
                        )
                    }
                }
            })?.setNegativeButton("Dismiss", DialogInterface.OnClickListener { dialog, _ -> dialog.cancel()
            })
        val alert = dialogBuilder?.create()
        alert?.setTitle("Item Comment:")
        alert?.show()
    }
}
