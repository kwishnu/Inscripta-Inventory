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
import com.baked.inscriptainventory.Activity.*
import com.baked.inscriptainventory.Adapter.InventoryAdapter
import com.baked.inscriptainventory.R
import com.baked.inscriptainventory.Resource.CallServer
import kotlinx.android.synthetic.main.fragment_layout.*
import com.baked.inscriptainventory.Activity.MainActivity.Companion.globalIPAddress
import com.google.android.material.snackbar.Snackbar

private const val TAG = "InscriptaInventory_F21"
private const val TAB = "12"
private lateinit var recyclerView: RecyclerView
private lateinit var itemsContainer: MutableList<MutableList<String>>

class Fragment21(
    private val items: MutableList<MutableList<String>>,
    private val images: MutableList<String>
) : Fragment() {
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
                MainActivity.globalDataArray.removeAt(DeleteItemActivity.itemIndexInGlobalArray)
            } else if (reason == "addItem") {
                itemsContainer.add(0, mutableListOf(imageNum, partNum, itemName, minStockLevel, numInStock, commentStr, "2"))
                MainActivity.globalDataArray.add(AddItemActivity.itemIndexInGlobalArray, mutableListOf(imageNum, partNum, itemName, minStockLevel, numInStock, commentStr, "2", "21"))
            } else if (reason == "changeCount") {
                itemsContainer[index.toInt()][4] = numInStock
                MainActivity.globalDataArray[ItemActionActivity.itemIndexInGlobalArray][4] = numInStock
            } else {
                itemsContainer[index.toInt()][0] = imageNum
                itemsContainer[index.toInt()][1] = partNum
                itemsContainer[index.toInt()][2] = itemName
                itemsContainer[index.toInt()][3] = minStockLevel
                itemsContainer[index.toInt()][4] = numInStock
                itemsContainer[index.toInt()][5] = commentStr

                MainActivity.globalDataArray[CallServer.itemIndexInGlobalArray][0] = imageNum
                MainActivity.globalDataArray[CallServer.itemIndexInGlobalArray][1] = partNum
                MainActivity.globalDataArray[CallServer.itemIndexInGlobalArray][2] = itemName
                MainActivity.globalDataArray[CallServer.itemIndexInGlobalArray][3] = minStockLevel
                MainActivity.globalDataArray[CallServer.itemIndexInGlobalArray][4] = numInStock
                MainActivity.globalDataArray[CallServer.itemIndexInGlobalArray][5] = commentStr
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }

        fun scrollToPosition(line: Int){
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(line, 0)
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
            showEditableComment(position)
        }

        fun numberClickListener(position: Int) {
            launchRequestItemActivity(position)
        }
        fun thumbnailClickListener(position: Int) {
            launchRequestItemActivity(position)
        }

        fun longClickListener(position: Int, view: View) {
            showPopUp(position, view)
        }

        val listener = { i: Int -> fragClickListener(i) }
        val imageListener = { i: Int -> imageClickListener(i) }
        val numberListener = { i: Int -> numberClickListener(i) }
        val thumbnailListener = { i: Int -> thumbnailClickListener(i) }
        val longClickListener = { i: Int, view: View -> longClickListener(i, view) }
        recyclerView.adapter = activity?.applicationContext?.let {
            InventoryAdapter(
                items,
                it,
                listener,
                imageListener,
                numberListener,
                thumbnailListener,
                longClickListener,
                images
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
        intent.putExtra("FromActivity", "Fragment")

        startActivityForResult(intent, 1)
    }

    private fun launchRequestItemActivity(pos: Int) {
        val intent = Intent(activity, RequestItemActivity::class.java)
        intent.putExtra("Image", items[pos][0])
        intent.putExtra("PartNum", items[pos][1])
        intent.putExtra("Item", items[pos][2])

        startActivity(intent)
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
                    intent.putExtra("Sheet", "21")
                    intent.putExtra("Row", (pos + 2).toString())
                    intent.putExtra("Comment", items[pos][5])
                    intent.putExtra("FromActivity", "Fragment")

                    startActivityForResult(intent, 1)
                }
                R.id.header2 -> {//Copy Item
                    MainActivity.globalImageIndex = items[pos][0]
                    MainActivity.globalPartNumber = items[pos][1]
                    MainActivity.globalItemName = items[pos][2]
                    MainActivity.globalMinStockLevel = items[pos][3]
                    MainActivity.globalStockCount = items[pos][4]
                    MainActivity.globalCommentStr= items[pos][5]
                    MainActivity.globalItemOnClipboard= true
                    Snackbar.make(rv, "Item copied to clipboard", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
                R.id.header3 -> {//Go to Delete Item Activity
                    val intent = Intent(activity, DeleteItemActivity::class.java)
                    intent.putExtra("Image", items[pos][0])
                    intent.putExtra("PartNum", items[pos][1])
                    intent.putExtra("Item", items[pos][2])
                    intent.putExtra("MinStockLevel", items[pos][3])
                    intent.putExtra("InStock", items[pos][4])
                    intent.putExtra("Sheet", "21")
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
        if (items[pos][5] != "null" && items[pos][5] != "") etComment.setText(items[pos][5])
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

                    var delimSheetNumStr = ""
                    var delimRowNumStr = ""

                    for (i in 0 until MainActivity.globalDataArray.size) {
                        if (MainActivity.globalDataArray[i][2] == items[pos][2] && MainActivity.globalDataArray[i][1] == items[pos][1]) {
                            delimSheetNumStr = MainActivity.globalDataArray[i][7] + "~" + delimSheetNumStr
                            delimRowNumStr = MainActivity.globalDataArray[i][6] + "~" + delimRowNumStr
                        }
                    }
                    val sheetResultStr = delimSheetNumStr.dropLastWhile { it.toString() == "~" }//Remove terminal ~ characters
                    val rowResultStr = delimRowNumStr.dropLastWhile { it.toString() == "~" }


                    context?.let {
                        CallServer(it).makeCall(
                            rv,//View
                            globalIPAddress,//ipAddressStr
                            "editItem",//Reason
                            items[pos][4],//Count
                            items[pos][1],//Part Number
                            items[pos][0],//Image
                            sheetResultStr,//Sheet
                            rowResultStr,//Row Number
                            "false",//Send Warning
                            items[pos][2],//Item Name
                            items[pos][3],//Min Stock Level
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
