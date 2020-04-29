package com.baked.inscriptainventory.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.baked.inscriptainventory.Adapter.ImageGridAdapter
import com.baked.inscriptainventory.Resource.CallServer
import com.baked.inscriptainventory.R
import kotlinx.android.synthetic.main.activity_delete_item.*
private const val TAG = "InscriptaInventory_DIA"
private const val STOCK_2 = "2"
private lateinit var tabArray: MutableList<String>
private lateinit var imagesArray: MutableList<String>

class DeleteItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var fromActivity = "Unknown"
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""
    private var imageIndex = "0"
    companion object SendReceive {
        operator fun invoke(sent: MutableList<String>) {
            tabArray = sent
        }
        fun sendReceiveImages(imagesSent: MutableList<String>) {
            imagesArray = imagesSent
        }
        var itemIndexInGlobalArray = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, tabArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner.isEnabled = false;
        sheetSelectSpinner.isClickable = false
        sheetSelectSpinner!!.adapter = aa

        val itemName = intent.getStringExtra("Item")
        imageIndex = intent.getStringExtra("Image")!!.toString()
        val itemPartNum = intent.getStringExtra("PartNum")
        val minStockLevel = intent.getStringExtra("MinStockLevel")
        val inStock = intent.getStringExtra("InStock")
        val sheetNum = intent.getStringExtra("Sheet")
        val rowNum = intent.getStringExtra("Row")
        fromActivity = intent.getStringExtra("FromActivity")!!.toString()
        ImageGridAdapter.setIGAIndex = imageIndex.toInt()

        descriptionEditText.setText(itemName)
        partNumberEditText.setText(itemPartNum)
        numInStockET.setText(inStock)
        minStockLevelET.setText(minStockLevel)
        sheetSelectSpinner.setSelection(sheetNum?.toInt()!! - 1)

        numInStockET.setOnFocusChangeListener() { v, event ->
            numInStockET.hint = if(numInStockET.hasFocus()) "" else STOCK_2
        }

        minStockLevelET.setOnFocusChangeListener() { v, event ->
            minStockLevelET.hint = if(minStockLevelET.hasFocus()) "" else STOCK_2
        }

        setAdapter(imagesArray)

        deleteButton.setOnClickListener {
            ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder
                .setMessage("Delete this item from InventoryXls.xslx?")
                .setCancelable(true)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, _ ->

                    for (i in 0 until MainActivity.globalDataArray.size) {
                        if (MainActivity.globalDataArray[i][7] != sheetNum) {
                            itemIndexInGlobalArray++
                        } else {
                            break
                        }
                    }
                    itemIndexInGlobalArray = itemIndexInGlobalArray + rowNum!!.toInt() - 1

                    CallServer(this).makeCall(
                            content,//View
                            ipAddressStr,//IP Address
                            "deleteItem",//Reason
                            "none",
                            "none",
                            "none",
                            sheetNum,
                            rowNum!!,
                            "false",//No need to send warning
                            "none",
                            "none",
                            "none"
                        )
                    deleteButton.isEnabled = false
                    deleteButton.setBackgroundColor(ContextCompat.getColor(this,
                        R.color.disabledGray
                    ))
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, _ -> dialog.cancel()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("Confirm Delete")
            alert.show()
        }
    }

    private fun setAdapter(images: MutableList<String>){
        fun imageClickListener(position: Int) {
            imageIndex = position.toString()
            image_rv.adapter?.notifyDataSetChanged()
        }
        val glm = StaggeredGridLayoutManager(2, GridLayoutManager.HORIZONTAL)
        image_rv.layoutManager = glm
        val imageListener = { i: Int -> imageClickListener(i) }
        val iga = ImageGridAdapter(this@DeleteItemActivity, images as ArrayList<String>, imageListener)
        image_rv.adapter = iga
        image_rv.adapter?.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d(TAG, "Spinner")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d(TAG, "Spinner Item Selected: " + tabArray[position])
    }
}
