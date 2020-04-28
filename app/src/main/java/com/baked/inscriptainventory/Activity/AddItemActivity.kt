package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.baked.inscriptainventory.Adapter.ImageGridAdapter
import com.baked.inscriptainventory.Resource.CallServer
import com.baked.inscriptainventory.R
import kotlinx.android.synthetic.main.activity_add_item.*

private const val TAG = "InscriptaInventory_AIA"
private const val STOCK_0 = "0"
private lateinit var tabArray: MutableList<String>
private lateinit var imagesArray: MutableList<String>

class AddItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""
    private var imageIndex = "0"
    private var sheetNum = "0"
    private var commentStr = "null"

    companion object SendReceive {//load arrays from MainActivity parseJson()
        operator fun invoke(sent: MutableList<String>) {
            tabArray = sent
        }
        fun sendReceiveImages(imagesSent: MutableList<String>) {
            imagesArray = imagesSent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)
        var partNumber = intent.getStringExtra("PartNum")
        val partNumNotNull = partNumber ?: ""
        partNumberEditText.setText(partNumNotNull)
        val currentTab = intent.getStringExtra("CurrentTab")
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        ImageGridAdapter.setIGAIndex = 0

        setAdapter(imagesArray)

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, tabArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner!!.adapter = aa
        sheetSelectSpinner.setSelection(currentTab?.toInt()!!)

        numInStockET.setOnFocusChangeListener { v, event ->
            numInStockET.hint = if (numInStockET.hasFocus()) "" else STOCK_0
        }
        minStockLevelET.setOnFocusChangeListener { v, event ->
            minStockLevelET.hint = if (minStockLevelET.hasFocus()) "" else STOCK_0
        }
        commentsImage.setOnClickListener {
            showCommentDialog()
        }
        addButton.setOnClickListener {
            val itemName = descriptionEditText.text.toString()
            partNumber = partNumberEditText.text.toString()
            if (descriptionEditText.text.isNullOrBlank()) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("""""Item Name" is required""")
                    .setCancelable(true)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Empty Field")
                alert.show()
                return@setOnClickListener
            }

            var nameExists = false
            var alreadyExists = false
            for (i in 0 until MainActivity.globalDataArray.size) {
                if (MainActivity.globalDataArray[i][2] == itemName) {
                    nameExists = true
                }
                if (MainActivity.globalDataArray[i][1] == partNumber) {
                    alreadyExists = true
                }
            }

            if (alreadyExists) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("This item already exists in inventory")
                    .setCancelable(true)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Duplicate Item")
                alert.show()
                return@setOnClickListener
            }

            if (nameExists) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("An item with this name already exists. Create item anyway?")
                    .setCancelable(true)
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()
                    })
                    .setPositiveButton("Create", DialogInterface.OnClickListener { dialog, _ ->
                        addItem(itemName)
                    })
                    val alert = dialogBuilder.create()
                alert.setTitle("Duplicate Name")
                alert.show()
                return@setOnClickListener
            } else {
                addItem(itemName)
            }
        }
    }

    fun addItem(item: String) {
        val numInStock =
            if (numInStockET.text.isNullOrBlank()) "0" else numInStockET.text.toString()
        val minStock =
            if (minStockLevelET.text.isNullOrBlank()) "0" else minStockLevelET.text.toString()
        val partNum =
            if (partNumberEditText.text.isNullOrBlank()) "None" else partNumberEditText.text.toString()
        MainActivity.globalDataArray.add(mutableListOf(imageIndex, partNum, item, minStock, numInStock, commentStr, "2", sheetNum))

        CallServer(this).makeCall(
            content,//View
            ipAddressStr,//IP Address
            "addItem",//Reason
            numInStock,
            partNum,
            imageIndex,
            sheetNum,
            "2",//row number -- not actually used, row is inserted after header
            "false",//No need to send warning
            item,
            minStock,
            commentStr
        )
        addButton.isEnabled = false
        addButton.setBackgroundColor(ContextCompat.getColor(this,
            R.color.disabledGray
        ))

    }

    @SuppressLint("InflateParams")
    fun showCommentDialog() {
        val view: View = layoutInflater.inflate(R.layout.dialog_edit_comment, null)
        val etComment = view.findViewById<View>(R.id.et_comment) as EditText
        if (commentStr != "null") etComment.setText(commentStr)
        val dialogBuilder = this.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        dialogBuilder
            .setMessage("")
            .setCancelable(true)
            .setView(view)
            .setPositiveButton("Save Comment", DialogInterface.OnClickListener { dialog, _ ->
                commentStr = etComment.text.toString()
            })
            .setNegativeButton("Dismiss", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Item Comment:")
        alert.show()
    }

    private fun setAdapter(images: MutableList<String>){
        fun imageClickListener(position: Int) {
            imageIndex = position.toString()
            image_rv.adapter?.notifyDataSetChanged()
        }
        val glm = StaggeredGridLayoutManager(2, GridLayoutManager.HORIZONTAL)
        image_rv.layoutManager = glm
        val imageListener = { i: Int -> imageClickListener(i) }
        val iga = ImageGridAdapter(this@AddItemActivity, images as ArrayList<String>, imageListener)
        image_rv.adapter = iga
        image_rv.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        ImageGridAdapter.setIGAIndex = 0
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d(TAG, "Spinner")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        sheetNum = position.toString()
    }
}
